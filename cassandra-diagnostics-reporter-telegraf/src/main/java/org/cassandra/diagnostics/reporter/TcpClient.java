package org.cassandra.diagnostics.reporter;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a simple TCP client using Java NIO.
 */
public class TcpClient {
    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);

    private static final String LOOP_THREAD_NAME = "tcp-client";
    private static final long INITIAL_RECONNECT_INTERVAL = 500;
    private static final long MAXIMUM_RECONNECT_INTERVAL = 30000;
    private static final int READ_BUFFER_SIZE = 0x100000;
    private static final int WRITE_BUFFER_SIZE = 0x100000;

    private long reconnectInterval = INITIAL_RECONNECT_INTERVAL;

    private ByteBuffer readBuf = ByteBuffer.allocateDirect(READ_BUFFER_SIZE);
    private ByteBuffer writeBuf = ByteBuffer.allocateDirect(WRITE_BUFFER_SIZE);

    private Thread thread;
    private SocketAddress address;

    private Selector selector;
    private SocketChannel channel;

    private final AtomicBoolean connected = new AtomicBoolean(false);

    /**
     * TCP client constructor.
     *
     * @param address the address (address:port) to connect to
     */
    public TcpClient(final SocketAddress address) {
        this.address = address;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loop();
            }
        });
        thread.setName(LOOP_THREAD_NAME);
    }

    /**
     * Starts the client.
     *
     * @throws IOException IOException
     */
    public void start() throws IOException {
        thread.start();
    }

    /**
     * Disconnects and stops the client.
     *
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public void stop() throws IOException, InterruptedException {
        thread.interrupt();
        selector.wakeup();
    }

    /**
     * Returns the connection status.
     * @return true if connected
     */
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * Sends the content of the given buffer.
     *
     * @param buffer data to send, the buffer should be flipped (ready for read)
     * @throws InterruptedException InterruptedException
     * @throws IOException IOException
     */
    public void send(ByteBuffer buffer) throws InterruptedException, IOException {
        if (!connected.get())
            throw new IOException("not connected");
        synchronized (writeBuf) {
            if (writeBuf.remaining() < buffer.remaining()) {
                writeBuf.flip();
                while (writeBuf.hasRemaining() && channel.write(writeBuf) > 0) {
                }
                writeBuf.compact();
            }

            if (Thread.currentThread().getId() != thread.getId()) {
                while (writeBuf.remaining() < buffer.remaining())
                    writeBuf.wait();
            } else {
                if (writeBuf.remaining() < buffer.remaining())
                    throw new IOException("send buffer full");
            }
            writeBuf.put(buffer);

            writeBuf.flip();
            while (writeBuf.hasRemaining() && channel.write(writeBuf) > 0) {
            }
            writeBuf.compact();

            if (writeBuf.hasRemaining()) {
                SelectionKey key = channel.keyFor(selector);
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        }
    }

    /**
     * Read event handler.
     *
     * @param buf buffer that contains data to be read
     */
    protected void onRead(ByteBuffer buf) {
    }

    /**
     * Connected event handler.
     */
    protected void onConnected() {
    }

    /**
     * Disconnect event handler.
     */
    protected void onDisconnected() {
    }

    private void configureChannel(SocketChannel channel) throws IOException {
        channel.configureBlocking(false);
        channel.socket().setSendBufferSize(0x100000);
        channel.socket().setReceiveBufferSize(0x100000);
        channel.socket().setKeepAlive(true);
        channel.socket().setReuseAddress(true);
        channel.socket().setSoLinger(false, 0);
        channel.socket().setSoTimeout(0);
        channel.socket().setTcpNoDelay(true);
    }

    private void loop() {
        try {
            while (!Thread.interrupted()) {
                try {
                    selector = Selector.open();
                    channel = SocketChannel.open();
                    configureChannel(channel);

                    channel.connect(address);
                    channel.register(selector, SelectionKey.OP_CONNECT);

                    while (!thread.isInterrupted() && channel.isOpen()) { // events multiplexing loop
                        if (selector.select() > 0)
                            processSelectedKeys(selector.selectedKeys());
                    }
                } catch (Exception e) {
                    logger.warn("An error occured", e);
                } finally {
                    connected.set(false);
                    onDisconnected();
                    writeBuf.clear();
                    readBuf.clear();
                    if (channel != null)
                        channel.close();
                    if (selector != null)
                        selector.close();
                }

                try {
                    Thread.sleep(reconnectInterval);
                    if (reconnectInterval < MAXIMUM_RECONNECT_INTERVAL)
                        reconnectInterval *= 2;
                    logger.info("Reconnecting to " + address.toString());
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Unrecoverable error", e);
        }
    }

    private void processSelectedKeys(@SuppressWarnings("rawtypes") Set keys) throws Exception {
        @SuppressWarnings("rawtypes")
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            SelectionKey key = (SelectionKey) itr.next();
            if (key.isReadable())
                processRead(key);
            if (key.isWritable())
                processWrite(key);
            if (key.isConnectable())
                processConnect(key);
            itr.remove();
        }
    }

    private void processConnect(SelectionKey key) throws Exception {
        SocketChannel ch = (SocketChannel) key.channel();
        if (ch.finishConnect()) {
            key.interestOps(key.interestOps() ^ SelectionKey.OP_CONNECT);
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            reconnectInterval = INITIAL_RECONNECT_INTERVAL;
            connected.set(true);
            onConnected();
        }
    }

    private void processRead(SelectionKey key) throws Exception {
        ReadableByteChannel ch = (ReadableByteChannel) key.channel();

        int bytesOp = 0, bytesTotal = 0;
        while (readBuf.hasRemaining() && (bytesOp = ch.read(readBuf)) > 0)
            bytesTotal += bytesOp;

        if (bytesTotal > 0) {
            readBuf.flip();
            onRead(readBuf);
            readBuf.compact();
        } else if (bytesOp == -1) {
            ch.close();
        }
    }

    private void processWrite(SelectionKey key) throws IOException {
        WritableByteChannel ch = (WritableByteChannel) key.channel();
        synchronized (writeBuf) {
            writeBuf.flip();

            int bytesOp = 0, bytesTotal = 0;
            while (writeBuf.hasRemaining() && (bytesOp = ch.write(writeBuf)) > 0)
                bytesTotal += bytesOp;

            if (writeBuf.remaining() == 0) {
                key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
            }

            if (bytesTotal > 0)
                writeBuf.notify();
            else if (bytesOp == -1) {
                ch.close();
            }

            writeBuf.compact();
        }
    }
}
