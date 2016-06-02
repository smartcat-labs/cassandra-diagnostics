package io.smartcat.cassandra.diagnostics.reporter;

import com.aphyr.riemann.Proto.Msg;
import com.aphyr.riemann.client.IRiemannClient;
import com.aphyr.riemann.client.RiemannClient;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.Reporter;
import io.smartcat.cassandra.diagnostics.ReporterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * A Riemann based {@link Reporter} implementation. Query reports are sending towards the configured Riemann server as
 * Riemann events.
 */
public class RiemannReporter extends Reporter {

    private static final String HOST_PROP = "riemannHost";

    private static final String PORT_PROP = "riemannPort";

    private static final String DEFAULT_PORT = "5555";

    private static final String SERVICE_NAME_PROP = "riemannServiceName";

    private static final String DEFAULT_SERVICE_NAME = "queryReport";

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RiemannReporter.class);

    private String serviceName;

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
    public void report(Query queryReport) {
        IRiemannClient client = riemannClient();
        if (client == null) {
            logger.warn("Cannot report riemann event without initialized client.");
            return;
        }

        logger.debug("Sending Query: execTime={}", queryReport.executionTimeInMilliseconds());
        try {
            sendEvent(queryReport);
        } catch (Exception e) {
            logger.debug("Sending Query failed, trying one more time: execTime={}, exception: {}",
                    queryReport.executionTimeInMilliseconds(), e.getMessage());
            retry(queryReport);
        }
    }

    private void retry(Query queryReport) {
        try {
            sendEvent(queryReport);
        } catch (IOException e) {
            logger.debug("Sending Query failed, ignoring message: execTime={}, exception: {}",
                    queryReport.executionTimeInMilliseconds(), e.getMessage());
        }
    }

    /**
     * Method which is sending event. Must be thread safe since deref is blocking until timeout, so if multiple threads
     * attempt to send event last one will win.
     *
     * @param queryReport Query to send
     * @return message of outcome.
     * @throws IOException
     */
    private synchronized Msg sendEvent(Query queryReport) throws IOException {
        Msg message = riemann.event()
                .service(serviceName)
                .state("ok")
                .metric(queryReport.executionTimeInMilliseconds())
                .ttl(30)
                .attribute("client", queryReport.clientAddress())
                .attribute("statement", queryReport.statement())
                .attribute("id", UUID.randomUUID().toString())
                .tag("id")
                .send()
                .deref(1, java.util.concurrent.TimeUnit.SECONDS);

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

        serviceName = config.options.getOrDefault(SERVICE_NAME_PROP, DEFAULT_SERVICE_NAME);
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
