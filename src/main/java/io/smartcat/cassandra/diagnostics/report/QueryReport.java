package io.smartcat.cassandra.diagnostics.report;

/**
 * This class represents a query report.
 */
public class QueryReport {
    /**
     * Query execution start time.
     */
    public long startTimeInMilliseconds;

    /**
     * Query execution time.
     */
    public long executionTimeInMilliseconds;

    /**
     * The originating client's address.
     */
    public String clientAddress;

    /**
     * CQL statement.
     */
    public String statement;

    /**
     * Constructor.
     *
     * @param startTimeInMilliseconds query execution start time
     * @param executionTimeInMilliseconds query execution time
     * @param clientAddress client address
     * @param statement query statement
     */
    public QueryReport(long startTimeInMilliseconds, long executionTimeInMilliseconds, String clientAddress,
            String statement) {
        super();
        this.startTimeInMilliseconds = startTimeInMilliseconds;
        this.executionTimeInMilliseconds = executionTimeInMilliseconds;
        this.clientAddress = clientAddress;
        this.statement = statement;
    }

    @Override
    public String toString() {
        return "QueryReport [startTimeInMilliseconds=" + startTimeInMilliseconds + ", executionTimeInMilliseconds="
                + executionTimeInMilliseconds + ", clientAddress=" + clientAddress + ", statement=" + statement + "]";
    }

}
