package io.smartcat.cassandra.diagnostics.connector;

import java.lang.instrument.Instrumentation;

import io.smartcat.cassandra.diagnostics.info.InfoProvider;

/**
 * Cassandra Diagnostics Connector.
 */
public interface Connector {
    /**
     * Performs Cassandra classes instrumentation in order to inject Cassandra Diagnostics
     * interceptors.
     *
     * @param inst          instrumentation reference
     * @param reporter      query reporter
     * @param configuration connector specific configuration
     */
    void init(Instrumentation inst, QueryReporter reporter, ConnectorConfiguration configuration);

    /**
     * Blocks the calling thread until the connector's target (e.g. Cassand node)
     * is fully setup so the rest of Cassandra Diagnostics initialization can be
     * safely completed without colliding with the target code.
     */
    void waitForSetupCompleted();

    /**
     * Get an info provider instance.
     *
     * @return info provider instance
     */
    InfoProvider getInfoProvider();
}
