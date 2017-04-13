package io.smartcat.cassandra.diagnostics.module;

import java.util.List;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;
import io.smartcat.cassandra.diagnostics.utils.Utils;

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
     * System hostname value.
     */
    protected final String hostname = Utils.getHostname();

    /**
     * System name under observation.
     */
    protected final String systemName = Utils.getSystemname();

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
     * Process an intercepted query.
     *
     * @param query Query object
     */
    public void process(Query query) {

    }

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
    public void stop() {

    }

}
