package io.smartcat.cassandra.diagnostics.report;

/**
 * Defines query reporter.
 */
public interface QueryReporter {

    /**
     * Accepts and handles the given report.
     *
     * @param queryReport query report
     */
    void report(QueryReport queryReport);
}
