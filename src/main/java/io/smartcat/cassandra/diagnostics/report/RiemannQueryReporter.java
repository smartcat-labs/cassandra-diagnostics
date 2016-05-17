package io.smartcat.cassandra.diagnostics.report;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aphyr.riemann.client.IRiemannClient;
import com.aphyr.riemann.client.RiemannClient;
<<<<<<< cf51227aed548cc86b4a5c32ad393be7631920fa
import com.google.inject.Inject;
=======
import io.smartcat.cassandra.diagnostics.config.ReporterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
>>>>>>> Added composite reporter, updated configuration, added ttl

import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * A Riemann based {@link QueryReporter} implementation. Query reports are sending towards the configured Riemann server
 * as Riemann events.
 */
public class RiemannQueryReporter implements QueryReporter {

    private static final String HOST_PROP = "riemannHost";

    private static final String PORT_PROP = "riemannPort";

    private static final String DEFAULT_PORT = "5555";

    private static final String SERVICE_NAME_PROP = "riemannServiceName";

    private static final String DEFAULT_SERVICE_NAME = "queryReport";

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RiemannQueryReporter.class);

    private ReporterConfiguration config;

    private String serviceName;

    private static IRiemannClient riemann;

    /**
     * Constructor.
     *
     * @param config configuration
     */
    public RiemannQueryReporter(ReporterConfiguration config) {
        this.config = config;
    }

    @Override
    public void report(QueryReport queryReport) {
        IRiemannClient client = riemannClient();
        if (client == null) {
            logger.warn("Cannot report riemann event without initialized client.");
            return;
        }

        logger.info("Sending QueryReport: execTime{}", queryReport.executionTimeInMilliseconds);
        riemann.event().service(serviceName).state("ok").metric(queryReport.executionTimeInMilliseconds).ttl(10)
                .attribute("client", queryReport.clientAddress).attribute("statement", queryReport.statement).send();
    }

    private IRiemannClient riemannClient() {
        if (riemann == null) {
            initRiemannClient(config);
        }

        return riemann;
    }

    private synchronized void initRiemannClient(ReporterConfiguration config) {
        if (riemann != null) {
            logger.warn("Riemann client already initialized");
            return;
        }

        logger.info("Initializing riemann client with config: " + config.toString());

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
