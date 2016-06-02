package io.smartcat.cassandra.diagnostics;

/**
 * Used by Cassandra Connector implementation to report intercepted queries.
 */
public abstract class Reporter {

    protected ReporterConfiguration configuration;

    /**
     * Constructor
     * @param configuration Reporter configuration
     */
    public Reporter(ReporterConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Reports an intercepted query.
     *
     * @param queryReport information about intecepted query
     */
    public abstract void report(Query queryReport);
}
