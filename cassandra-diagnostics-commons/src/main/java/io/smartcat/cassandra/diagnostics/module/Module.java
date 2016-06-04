package io.smartcat.cassandra.diagnostics.module;

import java.util.ArrayList;
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
    protected ModuleConfiguration configuration;

    /**
     * Reporter list defined in configuration.
     */
    public List<Reporter> reporters = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     */
    public Module(ModuleConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Process an intercepted query.
     *
     * @param query Query object
     */
    public abstract void process(Query query);

}
