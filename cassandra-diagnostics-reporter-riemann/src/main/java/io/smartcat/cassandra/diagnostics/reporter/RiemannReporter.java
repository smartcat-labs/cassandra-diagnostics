package io.smartcat.cassandra.diagnostics.reporter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aphyr.riemann.Proto.Msg;
import com.aphyr.riemann.client.EventDSL;
import com.aphyr.riemann.client.IRiemannClient;
import com.aphyr.riemann.client.RiemannClient;

import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * A Riemann based {@link Reporter} implementation. Query reports are sending towards the configured Riemann server as
 * Riemann events.
 */
public class RiemannReporter extends Reporter {

    private static final String HOST_PROP = "riemannHost";

    private static final String PORT_PROP = "riemannPort";

    private static final String DEFAULT_PORT = "5555";

    private static final String SERVICE_NAME_PROP = "riemannServiceName";

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RiemannReporter.class);

    private static IRiemannClient riemann;

    /**
     * Constructor.
     *
     * @param configuration configuration
     */
    public RiemannReporter(ReporterConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void report(Measurement measurement) {
        IRiemannClient client = riemannClient();
        if (client == null) {
            logger.warn("Cannot report riemann event without initialized client.");
            return;
        }

        logger.debug("Sending Measurement: time={}", measurement.time());
        try {
            sendEvent(measurement);
        } catch (Exception e) {
            logger.debug("Sending Query failed, trying one more time: execTime={}, exception: {}", measurement.time(),
                    e.getMessage());
            retry(measurement);
        }
    }

    private void retry(Measurement measurement) {
        try {
            sendEvent(measurement);
        } catch (IOException e) {
            logger.debug("Sending Query failed, ignoring message: execTime={}, exception: {}", measurement.time(),
                    e.getMessage());
        }
    }

    /**
     * Method which is sending event. Must be thread safe since deref is blocking until timeout, so if multiple threads
     * attempt to send event last one will win.
     *
     * @param measurement Measurement to send
     * @return message of outcome.
     * @throws IOException
     */
    private synchronized Msg sendEvent(Measurement measurement) throws IOException {
        final EventDSL event = riemann.event();
        event.service(measurement.name());
        event.state("ok");
        event.metric(measurement.value());
        event.ttl(30);
        for (Map.Entry<String, String> tag : measurement.tags().entrySet()) {
            event.tag(tag.getKey());
        }
        for (Map.Entry<String, String> field : measurement.fields().entrySet()) {
            event.attribute(field.getKey(), field.getValue());
        }

        Msg message = event.send().deref(1, TimeUnit.SECONDS);

        if (message == null || message.hasError()) {
            throw new IOException("Message timed out.");
        }

        if (message.hasError()) {
            throw new IOException(message.getError());
        }

        return message;
    }

    private IRiemannClient riemannClient() {
        if (riemann == null) {
            initRiemannClient(configuration);
        }

        return riemann;
    }

    private synchronized void initRiemannClient(ReporterConfiguration config) {
        if (riemann != null) {
            logger.warn("Riemann client already initialized");
            return;
        }

        logger.debug("Initializing riemann client with config: {}", config.toString());

        if (!config.options.containsKey(HOST_PROP)) {
            logger.warn("Tried to init Riemann client. Not properly configured. Aborting initialization.");
            return;
        }

        String host = config.options.get(HOST_PROP);
        int port = Integer.parseInt(config.options.getOrDefault(PORT_PROP, DEFAULT_PORT));
        try {
            riemann = RiemannClient.tcp(host, port);
            riemann.connect();
        } catch (IOException e) {
            logger.warn("Riemann client cannot be initialized", e);
        }
    }

}
