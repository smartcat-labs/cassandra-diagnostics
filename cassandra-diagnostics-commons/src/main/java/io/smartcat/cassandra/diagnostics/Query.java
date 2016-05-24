package io.smartcat.cassandra.diagnostics;

/**
 * This class represents a query report.
 */
public class Query {
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
     * @param startTimeInMilliseconds     query execution start time
     * @param executionTimeInMilliseconds query execution time
     * @param clientAddress               client address
     * @param statement                   query statement
     */
    public Query(long startTimeInMilliseconds, long executionTimeInMilliseconds, String clientAddress,
            String statement) {
        super();
        this.startTimeInMilliseconds = startTimeInMilliseconds;
        this.executionTimeInMilliseconds = executionTimeInMilliseconds;
        this.clientAddress = clientAddress;
        this.statement = statement;
    }

    @Override
    public String toString() {
        return "Query [startTimeInMilliseconds=" + startTimeInMilliseconds + ", executionTimeInMilliseconds="
                + executionTimeInMilliseconds + ", clientAddress=" + clientAddress + ", statement=" + statement + "]";
    }

}
