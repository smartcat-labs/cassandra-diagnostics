package io.smartcat.cassandra.diagnostics.query;

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

    /**
     * Defines possible consistency levels.
     */
    public enum ConsistencyLevel {
        /**
         * ANY consistency.
         */
        ANY,

        /**
         * ONE consistency.
         */
        ONE,

        /**
         * TWO consistency.
         */
        TWO,

        /**
         * THREE consistency.
         */
        THREE,

        /**
         * QUORUM consistency.
         */
        QUORUM,

        /**
         * ALL consistency.
         */
        ALL,

        /**
         * LOCAL_QUORUM consistency.
         */
        LOCAL_QUORUM,

        /**
         * EACH_QUORUM consistency.
         */
        EACH_QUORUM,

        /**
         * SERIAL consistency.
         */
        SERIAL,

        /**
         * LOCAL_SERIAL consistency.
         */
        LOCAL_SERIAL,

        /**
         * LOCAL_ONE consistency.
         */
        LOCAL_ONE,

        /**
         * Consistency level is unknown.
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
    private ConsistencyLevel consistencyLevel;

    /**
     * Query's execution start time.
     *
     * @return execution start timestamp
     */
    public long startTimeInMilliseconds() {
        return startTimeInMilliseconds;
    }

    /**
     * Query's execution time.
     *
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
     *
     * @return CQL statement type
     */
    public StatementType statementType() {
        return statementType;
    }

    /**
     * Query's key-space name.
     *
     * @return key space name
     */
    public String keyspace() {
        return keyspace;
    }

    /**
     * Query's table name (if applicable).
     *
     * @return table name
     */
    public String tableName() {
        return tableName;
    }

    /**
     * Query's key-space and table name (if applicable).
     *
     * @return full table name (keyspace.tableName)
     */
    public String fullTableName() {
        return keyspace + "." + tableName;
    }

    /**
     * CQL statement.
     *
     * @return CQL statement
     */
    public String statement() {
        return statement;
    }

    /**
     * Consistency level.
     *
     * @return consistencyLevel
     */
    public ConsistencyLevel consistencyLevel() {
        return consistencyLevel;
    }

    private Query(final long startTimeInMilliseconds, final long executionTimeInMilliseconds,
            final String clientAddress, final StatementType statementType, final String keyspace,
            final String tableName, final String statement, final ConsistencyLevel consistencyLevel) {
        this.startTimeInMilliseconds = startTimeInMilliseconds;
        this.executionTimeInMilliseconds = executionTimeInMilliseconds;
        this.clientAddress = clientAddress;
        this.statementType = statementType;
        this.keyspace = keyspace;
        this.tableName = tableName;
        this.statement = statement;
        this.consistencyLevel = consistencyLevel;
    }

    /**
     * Returns a new instance of Query.
     *
     * @param startTimeInMilliseconds     query execution's start time, given as epoch timestamp in milliseconds
     * @param executionTimeInMilliseconds query execution time in milliseconds
     * @param clientAddress               query's client socket address
     * @param statementType               type of query's statement
     * @param keyspace                    query's key space
     * @param tableName                   query's table name
     * @param statement                   query's CQL statement
     * @param consistencyLevel            query's consistencyLevel
     * @return a new Query instance
     */
    public static Query create(final long startTimeInMilliseconds, final long executionTimeInMilliseconds,
            final String clientAddress, final StatementType statementType, final String keyspace,
            final String tableName, final String statement, final ConsistencyLevel consistencyLevel) {
        return new Query(startTimeInMilliseconds, executionTimeInMilliseconds, clientAddress, statementType, keyspace,
                tableName, statement, consistencyLevel);
    }

    @Override
    public String toString() {
        return "Query [ " + "startTimeInMilliseconds=" + startTimeInMilliseconds + ", executionTimeInMilliseconds="
                + executionTimeInMilliseconds + ", clientAddress=" + clientAddress + ", statementType=" + statementType
                .name() + ", statement=" + statement + ", keyspace=" + keyspace + ", tableName=" + tableName  + ", "
                + "consistencyLevel=" + consistencyLevel.name() + " ]";
    }

}
