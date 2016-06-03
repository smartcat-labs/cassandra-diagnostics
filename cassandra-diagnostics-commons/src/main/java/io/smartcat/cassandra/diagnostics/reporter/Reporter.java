package io.smartcat.cassandra.diagnostics.reporter;

import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * Reporter abstraction that forces a constructor signature. All valid reporters should extend this class.
 */
public abstract class Reporter {

    /**
     * Reporter configuration.
     */
    protected ReporterConfiguration configuration;

    /**
     * Constructor.
     *
     * @param configuration Reporter configuration
     */
    public Reporter(ReporterConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Reports an intercepted query.
     *
     * @param measurement processed information about intercepted query
     */
    public abstract void report(Measurement measurement);
}
