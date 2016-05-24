package io.smartcat.cassandra.diagnostics.config;

/**
 * This class represents the Cassandra table.
 */
public class Table {

    /**
     * Keyspace for table.
     */
    public String keyspace;

    /**
     * A table name in keyspace.
     */
    public String table;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(", keyspace: \"").append(keyspace).append("\", table: ").append(table).append(" }");
        return sb.toString();
    }

}
