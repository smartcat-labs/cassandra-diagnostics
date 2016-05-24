package io.smartcat.cassandra.diagnostics;

/**
 * This class represents a query report.
 */
public class Query {
    /**
     * Query's execution start time.
     */
    public long startTimeInMilliseconds;

    /**
     * Query's execution time.
     */
    public long executionTimeInMilliseconds;

    /**
     * Query's execution error message (if any).
     */
    public String executionErrorMessage;

    /**
     * The originating client's socket address.
     */
    public String clientAddress;

    /**
     * CQL statement type.
     */
    public String statementType;

    /**
     * Query's key-space name.
     */
    public String keyspace;

    /**
     * Query's table name (if applicable).
     */
    public String tableName;

    /**
     * CQL statement.
     */
    public String statement;

    /**
     * Default constructor.
     */
    public Query() {
    }

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
        return "Query [ " +
                "startTimeInMilliseconds=" + startTimeInMilliseconds +
                ", executionTimeInMilliseconds=" + executionTimeInMilliseconds +
                ", clientAddress=" + clientAddress +
                ", statementType=" + statementType +
                ", statement=" + statement +
                ", keyspace=" + keyspace +
                ", tableName=" + tableName +
                ", executionErrorMessage=" + executionErrorMessage +
                " ]";
    }

}
