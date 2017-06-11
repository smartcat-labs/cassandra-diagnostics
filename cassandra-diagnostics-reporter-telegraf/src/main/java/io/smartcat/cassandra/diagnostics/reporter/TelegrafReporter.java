package io.smartcat.cassandra.diagnostics.reporter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * Reporter implementation for Telegraf.
 */
public class TelegrafReporter extends ReporterActor {

    private static final String HOST_PROP = "telegrafHost";

    private static final String PORT_PROP = "telegrafPort";

    private static final int DEFAULT_PORT = 8084;

    private static TcpClient telegrafClient;

    /**
     * Constructor.
     *
     * @param reporterName  Reporter class name
     * @param configuration Configuration
     */
    public TelegrafReporter(final String reporterName, final Configuration configuration) {
        super(reporterName, configuration);

        logger.debug("Initializing Telegraf reporter with config: {}", configuration.toString());

        if (!reporterConfiguration.options.containsKey(HOST_PROP)) {
            logger.warning(
                    "Telegraf reporter initialization failed. Missing required property {}. Aborting initialization.",
                    HOST_PROP);
            return;
        }

        final String host = reporterConfiguration.getOption(HOST_PROP);
        final int port = reporterConfiguration.getDefaultOption(PORT_PROP, DEFAULT_PORT);

        try {
            telegrafClient = new TcpClient(new InetSocketAddress(host, port)) {
                @Override
                protected void onConnected() {
                    logger.info("Telegraf client connected to {}:{}.", host, port);
                }

                @Override
                protected void onDisconnected() {
                    logger.info("Telegraf client disconnected from {}:{}.", host, port);
                }
            };
            telegrafClient.start();
        } catch (IOException e) {
            logger.warning("Telegraf reporter cannot be initialized", e);
        }
    }

    @Override
    protected void report(Measurement measurement) {
        if (telegrafClient == null || !telegrafClient.isConnected()) {
            logger.warning("Telegraf client is not connected. Skipping measurement {} with value {}.", measurement.name,
                    measurement.value);
            return;
        }

        logger.debug("Sending Measurement: name={}, value={}, time={}", measurement.name, measurement.value,
                measurement.time);
        try {
            sendEvent(measurement);
        } catch (Exception e) {
            logger.debug("Sending measurement failed: execTime={}, exception: {}", measurement.time, e.getMessage());
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
        ByteBuffer line = lineProtocol(measurement);
        if (line != null) {
            telegrafClient.send(line);
        }
    }

    @Override
    protected void stop() {
        try {
            telegrafClient.stop();
        } catch (IOException | InterruptedException e) {
            logger.error("Errored while stopping telegraf client", e);
        } finally {
            telegrafClient = null;
        }
    }

    private ByteBuffer lineProtocol(Measurement measurement) {
        try {
            final Point.Builder builder = Point.measurement(measurement.name);
            builder.time(measurement.time, TimeUnit.MILLISECONDS);

            builder.tag(measurement.tags);
            builder.tag("type", measurement.type.toString());

            if (measurement.isSimple()) {
                builder.addField("value", measurement.value);
            }

            for (Map.Entry<String, String> field : measurement.fields.entrySet()) {
                builder.addField(field.getKey(), field.getValue());
            }
            Charset charset = StandardCharsets.UTF_8;
            CharsetEncoder encoder = charset.newEncoder();
            return encoder.encode(CharBuffer.wrap(builder.build().lineProtocol().concat("\r\n").toCharArray()));
        } catch (Exception e) {
            logger.warning("Failed to send report to influx", e);
            return null;
        }
    }
}
