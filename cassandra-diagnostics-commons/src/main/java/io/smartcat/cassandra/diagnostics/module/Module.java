package io.smartcat.cassandra.diagnostics.module;

import java.util.List;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Module abstraction that forces a constructor signature. All valid modules should extend this class.
 */
public abstract class Module {

    /**
     * Module configuration.
     */
    protected final ModuleConfiguration configuration;

    /**
     * Reporter list defined in configuration.
     */
    protected final List<Reporter> reporters;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     */
    public Module(ModuleConfiguration configuration, List<Reporter> reporters) {
        this.configuration = configuration;
        this.reporters = reporters;
    }

    /**
     * Process an intercepted query if it is eligible for reporting and report measurement.
     *
     * @param query Query object
     */
    public void process(Query query) {
        if (isForReporting(query)) {
            Measurement measurement = transform(query);
            report(measurement);
        }
    }

    /**
     * Check if query is for reporting by this module.
     *
     * @param query Query object
     * @return if query is eligible for reporting or not
     */
    protected boolean isForReporting(Query query) {
        return true;
    }

    /**
     * Transforms query into module specific measurement.
     *
     * @param query Query object
     * @return measurement ready for reporting
     */
    protected abstract Measurement transform(Query query);

    /**
     * Report measurement on all configured reporters.
     *
     * @param measurement Measurement for reporting
     */
    protected void report(Measurement measurement) {
        for (Reporter reporter : reporters) {
            reporter.report(measurement);
        }
    }

    /**
     * Used to stop module with long running tasks.
     */
    protected void stop() {

    }

}
