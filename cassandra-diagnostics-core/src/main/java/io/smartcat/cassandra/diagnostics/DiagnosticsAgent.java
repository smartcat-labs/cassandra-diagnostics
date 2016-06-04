package io.smartcat.cassandra.diagnostics;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code DiagnosticAgent} acts as a Java agent used to instrument original Cassandra classes in order to extend them
 * with Cassandra Diagnostics additions.
 */
public class DiagnosticsAgent {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticsAgent.class);

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
    public static void premain(String args, Instrumentation inst) {
        logger.info("Cassandra Diagnostics starting.");
        Diagnostics diagnostics = new Diagnostics();
        ConnectorFactory.getImplementation().init(inst, diagnostics);
        logger.info("Cassandra Diagnostics initialized.");
    }

}
