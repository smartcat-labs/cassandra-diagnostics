package io.smartcat.cassandra.diagnostics.actor.messages;

/**
 * Query message definitions.
 */
public abstract class Query {

    /**
     * Query info provider reference.
     */
    public static final class InfoProviderRef extends Query {
    }

    /**
     * Query unreachable nodes.
     */
    public static final class UnreachableNodes extends Query {
    }

    /**
     * Query compaction settings info.
     */
    public static final class CompactionSettingsInfo extends Query {
    }

    /**
     * Query compactions info.
     */
    public static final class Compactions extends Query {
    }

    /**
     * Query thread pool stats.
     */
    public static final class TPStats extends Query {
    }

    /**
     * Query repair sessions.
     */
    public static final class RepairSessions extends Query {
    }

    /**
     * Query node info.
     */
    public static final class NodeInfo extends Query {
    }

}
