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
    public String name;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(", keyspace: \"").append(keyspace).append("\", tableName: ").append(name).append(" }");
        return sb.toString();
    }

}
