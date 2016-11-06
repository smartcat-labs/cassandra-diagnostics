package io.smartcat.cassandra.diagnostics.reporter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aphyr.riemann.client.EventDSL;
import com.aphyr.riemann.client.IRiemannClient;
import com.aphyr.riemann.client.RiemannBatchClient;
import com.aphyr.riemann.client.RiemannClient;
import com.aphyr.riemann.client.UnsupportedJVMException;

import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * A Riemann based {@link Reporter} implementation. Query reports are sending towards the configured Riemann server as
 * Riemann events.
 */
public class RiemannReporter extends Reporter {

    private static final Logger logger = LoggerFactory.getLogger(RiemannReporter.class);

    private static final String HOST_PROP = "riemannHost";

    private static final String PORT_PROP = "riemannPort";

    private static final String BATCH_EVENT_SIZE_PROP = "batchEventSize";

    private static final String DEFAULT_PORT = "5555";

    private static final String DEFAULT_BATCH_EVENT_SIZE = "10";

    private static IRiemannClient riemannClient;

    /**
     * Constructor.
     *
     * @param configuration configuration
     */
    public RiemannReporter(ReporterConfiguration configuration) {
        super(configuration);

        logger.debug("Initializing riemann client with config: {}", configuration.toString());

        if (!configuration.options.containsKey(HOST_PROP)) {
            logger.warn("Tried to init Riemann client. Not properly configured. Aborting initialization.");
            return;
        }

        String host = configuration.options.get(HOST_PROP);
        int port = Integer.parseInt(configuration.getDefaultOption(PORT_PROP, DEFAULT_PORT));
        int batchEventSize = Integer
                .parseInt(configuration.getDefaultOption(BATCH_EVENT_SIZE_PROP, DEFAULT_BATCH_EVENT_SIZE));

        try {
            riemannClient = new RiemannBatchClient(RiemannClient.tcp(new InetSocketAddress(host, port)),
                    batchEventSize);
            riemannClient.connect();
        } catch (IOException e) {
            logger.warn("Riemann client cannot be initialized", e);
        } catch (UnsupportedJVMException e) {
            logger.warn("Riemann Batch client not supported, faling back to riemann client.");
            try {
                riemannClient = RiemannClient.tcp(new InetSocketAddress(host, port));
            } catch (IOException e1) {
                logger.warn("Riemann client cannot be initialized", e);
            }
        }
    }

    @Override
    public void report(Measurement measurement) {
        if (!riemannClient.isConnected()) {
            logger.warn("Riemann client dropped connection, reconnecting.");
            try {
                riemannClient.reconnect();
            } catch (IOException e) {
                logger.warn("Cannot reconnect, skipping measurement {} with value {}.", measurement.name(),
                        measurement.value());
                return;
            }
        }

        logger.debug("Sending Measurement: name={}, value={}, time={}", measurement.name(), measurement.value(),
                measurement.time());
        try {
            sendEvent(measurement);
        } catch (Exception e) {
            logger.debug("Sending Query failed, trying one more time: execTime={}, exception: {}", measurement.time(),
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
        event.service(measurement.name());
        event.state("ok");
        event.metric(measurement.value());
        event.time(measurement.time());
        event.ttl(30);
        for (Map.Entry<String, String> tag : measurement.tags().entrySet()) {
            event.tag(tag.getKey());
            event.attribute(tag.getKey(), tag.getValue());
        }
        for (Map.Entry<String, String> field : measurement.fields().entrySet()) {
            event.attribute(field.getKey(), field.getValue());
        }

        riemannClient.sendEvent(event.build());
    }

    @Override
    public void stop() {
        riemannClient.close();
    }

}
