package io.smartcat.cassandra.diagnostics.connector;

import java.lang.instrument.Instrumentation;

/**
 * Cassandra Diagnostics Connector.
 */
public interface Connector {
    /**
     * Performs Cassandra classes instrumentation in order to inject Cassandra Diagnostics
     * interceptors.
     *
     * @param inst     instrumentation reference
     * @param reporter query reporter
     */
    void init(Instrumentation inst, QueryReporter reporter);

    /**
     * Blocks the calling thread until the connector's target (e.g. Cassand node)
     * is fully setup so the rest of Cassandra Diagnostics initialization can be
     * safely completed without colliding with the target code.
     */
    void waitForSetupCompleted();
}
