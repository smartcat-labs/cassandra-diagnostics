package io.smartcat.cassandra.diagnostics.module;

import java.util.List;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Slow query module providing reports of query execution times over a defined threshold.
 */
public class SlowQueryModule extends Module {

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     */
    public SlowQueryModule(ModuleConfiguration configuration, List<Reporter> reporters) {
        super(configuration, reporters);
    }

    @Override
    public void process(Query query) {

    }
}
