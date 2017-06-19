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

    private static Diagnostics diagnostics;

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
        diagnostics = new Diagnostics();
    }

}
