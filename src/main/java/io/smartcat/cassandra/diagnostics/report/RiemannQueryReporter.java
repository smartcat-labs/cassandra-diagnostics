package io.smartcat.cassandra.diagnostics.report;

import com.aphyr.riemann.client.IRiemannClient;
import com.aphyr.riemann.client.RiemannClient;
import com.google.inject.Inject;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A Riemann based {@link QueryReporter} implementation. Query reports
 * are sending towards the configured Riemann server as Riemann events.
 */
public class RiemannQueryReporter implements QueryReporter {

    private static final String HOST_PROP = "riemannHost";

    private static final String PORT_PROP = "riemannPort";

    private static final String DEFAULT_PORT = "5555";

    private static final String BATCH_SIZE_PROP = "riemannBatchSize";

    private static final String DEFAULT_BATCH_SIZE = "10";

    private static final String SERVICE_NAME_PROP = "riemannServiceName";

    private static final String DEFAULT_SERVICE_NAME = "queryReport";

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RiemannQueryReporter.class);

    private Configuration config;

    private String serviceName;

    private static IRiemannClient riemann;

    /**
     * Constructor.
     *
     * @param config configuration
     */
    @Inject
    public RiemannQueryReporter(Configuration config) {
        this.config = config;
    }

    @Override
    public void report(QueryReport queryReport) {
        IRiemannClient client = riemannClient();
        if (client == null) {
            logger.warn("Trying to report riemann event without initialized client. Exiting...");
            return;
        }

        logger.debug("Sending QueryReport: execTime=" + queryReport.executionTime);
        riemannClient().event()
                .service(serviceName)
                .state("ok")
                //.time(queryReport.startTime / 1000000)
                .metric(queryReport.executionTime / 1000000f) // Log in ms so that we don't drown in high numbers
                .attribute("client", queryReport.clientAddress)
                .attribute("statement", queryReport.statement)
                .send();
    }

    private IRiemannClient riemannClient() {
        if (riemann == null) {
            initRiemannClient(config);
            serviceName = config.reporterOptions.getOrDefault(SERVICE_NAME_PROP, DEFAULT_SERVICE_NAME);
        }
        return riemann;
    }

    private static synchronized void initRiemannClient(Configuration config) {
        logger.debug("Initializing riemann client with config: " + config.toString());

        if (!config.reporterOptions.containsKey(HOST_PROP)) {
            logger.warn("Tried to init Riemann client. Not properly configured. Aborting initialization.");
            return;
        }

        String host = config.reporterOptions.get(HOST_PROP);
        int port = Integer.parseInt(config.reporterOptions.getOrDefault(PORT_PROP, DEFAULT_PORT));
        int batchSize = Integer.parseInt(
                config.reporterOptions.getOrDefault(BATCH_SIZE_PROP, DEFAULT_BATCH_SIZE));
        try {
            riemann = RiemannClient.tcp(host, port);
            riemann.connect();
        } catch (IOException e) {
            logger.warn("Riemann client cannot be initialized", e);
        }
    }

}
