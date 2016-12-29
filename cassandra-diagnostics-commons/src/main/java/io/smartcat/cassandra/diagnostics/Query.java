package io.smartcat.cassandra.diagnostics;

/**
 * This class represents a query report.
 */
public class Query {

    /**
     * Defines possible statement types.
     */
    public enum StatementType {
        /**
         * SELECT statement.
         */
        SELECT,

        /**
         * UPDATE/INSERT statement.
         */
        UPDATE,

        /**
         * Statement type unknown.
         */
        UNKNOWN
    }

    private long startTimeInMilliseconds;
    private long executionTimeInMilliseconds;
    private String clientAddress;
    private StatementType statementType;
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
     * The originating client's socket address.
     *
     * @return client's socket address
     */
    public String clientAddress() {
        return clientAddress;
    }

    /**
     * CQL statement type.
     * @return CQL statement type
     */
    public StatementType statementType() {
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
     * Query's key-space and table name (if applicable).
     * @return full table name (keyspace.tableName)
     */
    public String fullTableName() {
        return keyspace + "." + tableName;
    }

    /**
     * CQL statement.
     * @return CQL statement
     */
    public String statement() {
        return statement;
    }

    private Query(final long startTimeInMilliseconds, final long executionTimeInMilliseconds,
            final String clientAddress, final StatementType statementType, final String keyspace,
            final String tableName, final String statement) {
        this.startTimeInMilliseconds = startTimeInMilliseconds;
        this.executionTimeInMilliseconds = executionTimeInMilliseconds;
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
     * @return a new Query instance
     */
    public static Query create(final long startTimeInMilliseconds, final long executionTimeInMilliseconds,
            final String clientAddress, final StatementType statementType, final String keyspace,
            final String tableName, final String statement) {
        return new Query(startTimeInMilliseconds, executionTimeInMilliseconds,
                clientAddress, statementType, keyspace, tableName, statement);
    }

    @Override
    public String toString() {
        return "Query [ " +
                "startTimeInMilliseconds=" + startTimeInMilliseconds +
                ", executionTimeInMilliseconds=" + executionTimeInMilliseconds +
                ", clientAddress=" + clientAddress +
                ", statementType=" + statementType.name() +
                ", statement=" + statement +
                ", keyspace=" + keyspace +
                ", tableName=" + tableName +
                " ]";
    }

}
