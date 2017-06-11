package io.smartcat.cassandra.diagnostics.actor.messages;

/**
 * Command message definitions.
 */
public abstract class Command {

    /**
     * Graceful shutdown message.
     */
    public static final class GracefulShutdown extends Command {
    }

    /**
     * Start message.
     */
    public static final class Start extends Command {
    }

    /**
     * Stop message.
     */
    public static final class Stop extends Command {
    }

}
