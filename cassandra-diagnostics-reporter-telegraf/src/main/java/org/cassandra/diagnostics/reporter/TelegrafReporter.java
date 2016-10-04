package org.cassandra.diagnostics.reporter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

/**
 * Reporter implementation for Telegraf.
 */
public class TelegrafReporter extends Reporter {
    private static final String HOST_PROP = "telegrafHost";

    private static final String PORT_PROP = "telegrafPort";

    private static final String DEFAULT_PORT = "8084";

    private static final Logger logger = LoggerFactory.getLogger(TelegrafReporter.class);

    private static TcpClient telegrafClient;

    /**
     * Constructor.
     *
     * @param configuration configuration
     */
    public TelegrafReporter(ReporterConfiguration configuration) {
        super(configuration);

        logger.debug("Initializing Telegraf reporter with config: {}", configuration.toString());

        if (!configuration.options.containsKey(HOST_PROP)) {
            logger.warn("Telegraf reporter initialization failed. Missing required property " +
                    HOST_PROP + ". Aborting initialization.");
            return;
        }

        final String host = configuration.options.get(HOST_PROP);
        final int port = Integer.parseInt(configuration.getDefaultOption(PORT_PROP, DEFAULT_PORT));

        try {
            telegrafClient = new TcpClient(new InetSocketAddress(host, port)) {
                @Override
                protected void onConnected() {
                    logger.info("Telegraf client connected to " + host + ":" + port);
                }

                @Override
                protected void onDisconnected() {
                    logger.info("Telegraf client disconnected from " + host + ":" + port);
                }
            };
            telegrafClient.start();
        } catch (IOException e) {
            logger.warn("Telegraf reporter cannot be initialized", e);
        }
    }

    @Override
    public void report(Measurement measurement) {
        if (telegrafClient == null || !telegrafClient.isConnected()) {
            logger.warn("Telegraf client is not connected. Skipping measurement {} with value {}.",
                    measurement.name(), measurement.value());
           // return;
        }

        logger.debug("Sending Measurement: name={}, value={}, time={}", measurement.name(), measurement.value(),
                measurement.time());
        try {
            sendEvent(measurement);
        } catch (Exception e) {
            logger.debug("Sending measurement failed: execTime={}, exception: {}", measurement.time(),
                    e.getMessage());
        }
    }

    /**
     * Sends the given event.
     *
     * @param measurement Measurement to send
     * @throws IOException
     * @throws InterruptedException
     */
    private void sendEvent(Measurement measurement) throws IOException, InterruptedException {
        StringBuilder buf = new StringBuilder();
        buf.append(measurement.name());

        for (Map.Entry<String, String> tag : measurement.tags().entrySet()) {
            buf.append(",");
            buf.append(tag.getKey());
            buf.append("=");
            buf.append(tag.getValue());
        }

        buf.append(" value=");
        buf.append(measurement.value());

        for (Map.Entry<String, String> field : measurement.fields().entrySet()) {
            buf.append(",");
            buf.append(field.getKey());
            buf.append("=");
            buf.append(field.getValue());
        }

        buf.append(" ");
        buf.append(measurement.time());
        buf.append("\r\n");

        Charset charset = StandardCharsets.UTF_8;
        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer bytes = encoder.encode(CharBuffer.wrap(buf));

        telegrafClient.send(bytes);
    }

}
