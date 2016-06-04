package io.smartcat.cassandra.diagnostics.module;

import java.util.List;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Heartbeat module providing logged heartbeats at defined intervals.
 */
public class HeartbeatModule extends Module {

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     */
    public HeartbeatModule(ModuleConfiguration configuration, List<Reporter> reporters) {
        super(configuration, reporters);
    }

    @Override
    public void process(Query query) {

    }
}
