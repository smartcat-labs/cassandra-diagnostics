package io.smartcat.cassandra.diagnostics.actor.messages;

/**
 * Notification message definitions.
 */
public abstract class Notification {

    /**
     * Cassandra startup complete message.
     */
    public static final class CassandraStartupComplete extends Notification {
    }

}
