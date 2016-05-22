package io.smartcat.cassandra.diagnostics;

import io.smartcat.cassandra.diagnostics.connector.Query;

/**
 * Used by Cassandra Connector implementation to report intercepted queries.
 */
public interface Reporter {
    /**
     * Reports an intercepted query.
     * @param queryReport information about intecepted query
     */
    void report(Query queryReport);
}
