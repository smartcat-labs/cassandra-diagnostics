package io.smartcat.cassandra.diagnostics.connector;

import io.smartcat.cassandra.diagnostics.Query;

/**
 * Interface used by {@link Connector} implementation to report an intercepted query.
 */
public interface QueryReporter {
    /**
     * Reports an intercepted query.
     *
     * @param query intercepted query
     */
    void report(Query query);
}
