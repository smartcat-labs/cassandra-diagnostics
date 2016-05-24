package io.smartcat.cassandra.diagnostics.connector;

import java.lang.instrument.Instrumentation;

/**
 * Cassandra Diagnostics Connector.
 */
public interface Connector {
    /**
     * Performs Cassandra classes instrumentation in order to inject Cassandra Diagnostics.
     *
     * @param inst     instrumentation reference
     * @param reporter query reporter
     */
    void init(Instrumentation inst, QueryReporter reporter);
}
