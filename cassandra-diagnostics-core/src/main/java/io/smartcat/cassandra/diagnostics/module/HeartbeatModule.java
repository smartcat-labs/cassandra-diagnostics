package io.smartcat.cassandra.diagnostics.module;

import io.smartcat.cassandra.diagnostics.Query;

/**
 * Heartbeat module providing logged heartbeats at defined intervals.
 */
public class HeartbeatModule extends Module {

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     */
    public HeartbeatModule(ModuleConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void process(Query query) {

    }
}
