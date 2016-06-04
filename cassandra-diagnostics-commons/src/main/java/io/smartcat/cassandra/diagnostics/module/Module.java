package io.smartcat.cassandra.diagnostics.module;

import java.util.List;

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
     * Process an intercepted query.
     *
     * @param query Query object
     */
    public abstract void process(Query query);

}
