package io.smartcat.cassandra.diagnostics;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.connector.Connector;

/**
 * {@code DiagnosticAgent} acts as a Java agent used to instrument original Cassandra classes in order to extend them
 * with Cassandra Diagnostics additions.
 */
public class DiagnosticsAgent {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticsAgent.class);

    private static final String INITIALIZATION_THREAD_NAME = "cassandra-diagnostics-agent";

    /**
     * Prevents class instantiation.
     */
    private DiagnosticsAgent() {
    }

    /**
     * Entry point for agent when it is started upon VM start.
     *
     * @param args agent arguments
     * @param inst instrumentation handle
     */
    public static void premain(final String args, final Instrumentation inst) {
        logger.info("Cassandra Diagnostics starting.");
        final Diagnostics diagnostics = new Diagnostics();
        final Connector connector = ConnectorFactory.getImplementation();
        connector.init(inst, diagnostics, diagnostics.getConfiguration().connector);
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                connector.waitForSetupCompleted();
                diagnostics.activate();
                logger.info("Cassandra Diagnostics initialized.");
            }
        });
        th.setName(INITIALIZATION_THREAD_NAME);
        th.setDaemon(true);
        th.start();
    }
}
