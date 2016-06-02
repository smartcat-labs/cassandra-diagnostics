package io.smartcat.cassandra.diagnostics;

/**
 * This class represents a query report.
 */
public class Query {

    private long startTimeInMilliseconds;
    private long executionTimeInMilliseconds;
    private String executionErrorMessage;
    private String clientAddress;
    private String statementType;
    private String keyspace;
    private String tableName;
    private String statement;

    /**
     * Query's execution start time.
     * @return execution start timestamp
     */
    public long startTimeInMilliseconds() {
        return startTimeInMilliseconds;
    }

    /**
     * Query's execution time.
     * @return execution time
     */
    public long executionTimeInMilliseconds() {
        return executionTimeInMilliseconds;
    }

    /**
     * Query's execution error message (if any).
     * @return error message
     */
    public String executionErrorMessage() {
        return executionErrorMessage;
    }

    /**
     * The originating client's socket address.
     * @return client's socket address
     */
    public String clientAddress() {
        return clientAddress;
    }

    /**
     * CQL statement type.
     * @return CQL statement type
     */
    public String statementType() {
        return statementType;
    }

    /**
     * Query's key-space name.
     * @return key space name
     */
    public String keyspace() {
        return keyspace;
    }

    /**
     * Query's table name (if applicable).
     * @return table name
     */
    public String tableName() {
        return tableName;
    }

    /**
     * CQL statement.
     * @return CQL statement
     */
    public String statement() {
        return statement;
    }

    private Query(long startTimeInMilliseconds, long executionTimeInMilliseconds, String clientAddress,
            String statementType, String keyspace, String tableName, String statement, String executionErrorMessage) {
        this.startTimeInMilliseconds = startTimeInMilliseconds;
        this.executionTimeInMilliseconds = executionTimeInMilliseconds;
        this.executionErrorMessage = executionErrorMessage;
        this.clientAddress = clientAddress;
        this.statementType = statementType;
        this.keyspace = keyspace;
        this.tableName = tableName;
        this.statement = statement;
    }

    /**
     * Returns a new instance of Query.
     *
     * @param startTimeInMilliseconds query execution's start time, given as epoch timestamp in milliseconds
     * @param executionTimeInMilliseconds query execution time in milliseconds
     * @param clientAddress query's client socket address
     * @param statementType type of query's statement
     * @param keyspace query's key space
     * @param tableName query's table name
     * @param statement query's CQL statement
     * @param executionErrorMessage query's execution error message
     * @return a new Query instance
     */
    public static Query create(final long startTimeInMilliseconds, final long executionTimeInMilliseconds,
            final String clientAddress, final String statementType, final String keyspace, final String tableName,
            final String statement, final String executionErrorMessage) {
        return new Query(startTimeInMilliseconds, executionTimeInMilliseconds,
            clientAddress, statementType, keyspace, tableName, statement, executionErrorMessage);
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
