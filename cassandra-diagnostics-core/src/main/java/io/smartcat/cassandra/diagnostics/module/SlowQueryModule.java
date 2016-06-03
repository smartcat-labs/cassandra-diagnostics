package io.smartcat.cassandra.diagnostics.module;

import io.smartcat.cassandra.diagnostics.Query;

/**
 * Slow query module providing reports of query execution times over a defined threshold.
 */
public class SlowQueryModule extends Module {

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     */
    public SlowQueryModule(ModuleConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void process(Query query) {

    }
}
