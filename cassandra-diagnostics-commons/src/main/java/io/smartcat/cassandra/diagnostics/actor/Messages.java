package io.smartcat.cassandra.diagnostics.actor;

/**
 * Actor typed messages.
 */
public final class Messages {

    public interface Command {
    }

    public static final class GracefulShutdown implements Command {
    }

    public static final class Start implements Command {
    }

    public static final class Stop implements Command {
    }
}
