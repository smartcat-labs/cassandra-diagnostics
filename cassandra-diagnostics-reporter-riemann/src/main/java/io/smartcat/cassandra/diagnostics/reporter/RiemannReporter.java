package io.smartcat.cassandra.diagnostics.reporter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import io.riemann.riemann.client.EventDSL;
import io.riemann.riemann.client.IRiemannClient;
import io.riemann.riemann.client.RiemannBatchClient;
import io.riemann.riemann.client.RiemannClient;
import io.riemann.riemann.client.UnsupportedJVMException;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * A Riemann based {@link ReporterActor} implementation. Query reports are sending towards the configured Riemann server
 * as Riemann events.
 */
public class RiemannReporter extends ReporterActor {

    private static final String HOST_PROP = "riemannHost";

    private static final String PORT_PROP = "riemannPort";

    private static final String BATCH_EVENT_SIZE_PROP = "batchEventSize";

    private static final int DEFAULT_PORT = 5555;

    private static final int DEFAULT_BATCH_EVENT_SIZE = 10;

    private static IRiemannClient riemannClient;

    /**
     * Constructor.
     *
     * @param reporterName  Reporter class name
     * @param configuration Configuration
     */
    public RiemannReporter(final String reporterName, final Configuration configuration) {
        super(reporterName, configuration);

        logger.debug("Initializing riemann client with config: {}", configuration.toString());

        if (!reporterConfiguration.options.containsKey(HOST_PROP)) {
            logger.warning("Tried to init Riemann client. Not properly configured. Aborting initialization.");
            return;
        }

        final String host = reporterConfiguration.getOption(HOST_PROP);
        final int port = reporterConfiguration.getDefaultOption(PORT_PROP, DEFAULT_PORT);
        final int batchEventSize = reporterConfiguration
                .getDefaultOption(BATCH_EVENT_SIZE_PROP, DEFAULT_BATCH_EVENT_SIZE);

        try {
            riemannClient = new RiemannBatchClient(RiemannClient.tcp(new InetSocketAddress(host, port)),
                    batchEventSize);
            riemannClient.connect();
        } catch (IOException e) {
            logger.warning("Riemann client cannot be initialized", e);
        } catch (UnsupportedJVMException e) {
            logger.warning("Riemann Batch client not supported, faling back to riemann client.");
            try {
                riemannClient = RiemannClient.tcp(new InetSocketAddress(host, port));
            } catch (IOException e1) {
                logger.warning("Riemann client cannot be initialized", e);
            }
        }
    }

    @Override
    protected void report(Measurement measurement) {
        if (!riemannClient.isConnected()) {
            logger.warning("Riemann client dropped connection, reconnecting.");
            try {
                riemannClient.reconnect();
            } catch (IOException e) {
                logger.warning("Cannot reconnect, skipping measurement {} with value {}.", measurement.name,
                        measurement.value);
                return;
            }
        }

        logger.debug("Sending Measurement: name={}, value={}, time={}", measurement.name, measurement.value,
                measurement.time);
        try {
            sendEvent(measurement);
        } catch (Exception e) {
            logger.debug("Sending Query failed, trying one more time: execTime={}, exception: {}", measurement.time,
                    e.getMessage());
        }
    }

    /**
     * Method which is sending event.
     *
     * @param measurement Measurement to send
     * @throws IOException
     */
    private void sendEvent(Measurement measurement) throws IOException {
        final EventDSL event = riemannClient.event();
        event.service(measurement.name);
        event.state("ok");
        if (measurement.isSimple()) {
            event.metric(measurement.value);
        }
        event.time(measurement.time);
        event.ttl(30);
        for (Map.Entry<String, String> tag : measurement.tags.entrySet()) {
            event.tag(tag.getKey());
            event.attribute(tag.getKey(), tag.getValue());
        }
        event.tag("type");
        event.attribute("type", measurement.type.toString());
        for (Map.Entry<String, String> field : measurement.fields.entrySet()) {
            event.attribute(field.getKey(), field.getValue());
        }

        riemannClient.sendEvent(event.build());
    }

    @Override
    protected void stop() {
        riemannClient.close();
    }

}
