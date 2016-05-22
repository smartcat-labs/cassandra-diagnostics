package io.smartcat.cassandra.diagnostics.connector;

/**
 * Interface used by {@link Connector} implementation to report an intercepted query.
 */
@FunctionalInterface
public interface QueryReporter {
    /**
     * Reports an intercepted query.
     * @param query intercepted query
     */
    void report(Query query);
}
