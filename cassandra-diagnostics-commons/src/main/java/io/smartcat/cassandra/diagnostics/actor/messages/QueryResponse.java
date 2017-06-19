package io.smartcat.cassandra.diagnostics.actor.messages;

import java.util.List;

import akka.actor.ActorRef;
import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.CompactionSettingsInfo;
import io.smartcat.cassandra.diagnostics.info.NodeInfo;
import io.smartcat.cassandra.diagnostics.info.TPStatsInfo;

/**
 * Query response message definitions.
 */
public abstract class QueryResponse {

    /**
     * Info provider reference query response.
     */
    public static final class InfoProviderRef extends QueryResponse {

        /**
         * Info provider field.
         */
        public final ActorRef infoProvider;

        /**
         * Constructor.
         *
         * @param infoProvider info provider
         */
        public InfoProviderRef(final ActorRef infoProvider) {
            this.infoProvider = infoProvider;
        }
    }

    /**
     * Unreachable nodes query response.
     */
    public static final class UnreachableNodesResp extends QueryResponse {

        /**
         * Unreachable nodes field.
         */
        public final List<String> unreachableNodes;

        /**
         * Constructor.
         *
         * @param unreachableNodes unreachable nodes
         */
        public UnreachableNodesResp(final List<String> unreachableNodes) {
            this.unreachableNodes = unreachableNodes;
        }
    }

    /**
     * Compactions info query response.
     */
    public static final class CompactionsResp extends QueryResponse {

        /**
         * Comapction settings info.
         */
        public final CompactionSettingsInfo compactionSettingsInfo;

        /**
         * Compaction info.
         */
        public final List<CompactionInfo> compactionInfo;

        /**
         * Constructor.
         *
         * @param compactionSettingsInfo compaction settings info
         * @param compactionInfo         compaction info
         */
        public CompactionsResp(final CompactionSettingsInfo compactionSettingsInfo,
                final List<CompactionInfo> compactionInfo) {
            this.compactionInfo = compactionInfo;
            this.compactionSettingsInfo = compactionSettingsInfo;
        }
    }

    /**
     * Thread pool stats query response.
     */
    public static final class TPStatsResp extends QueryResponse {

        /**
         * TP stats info.
         */
        public final List<TPStatsInfo> tpStatsInfo;

        /**
         * Constructor.
         *
         * @param tpStatsInfo TP stats info
         */
        public TPStatsResp(final List<TPStatsInfo> tpStatsInfo) {
            this.tpStatsInfo = tpStatsInfo;
        }
    }

    /**
     * Repair sessions query response.
     */
    public static final class RepairSessionsResp extends QueryResponse {

        /**
         * Repair sessions.
         */
        public final long repairSessions;

        /**
         * Constructor.
         *
         * @param repairSessions repair sessions
         */
        public RepairSessionsResp(final long repairSessions) {
            this.repairSessions = repairSessions;
        }
    }

    /**
     * Node info query response.
     */
    public static final class NodeInfoResp extends QueryResponse {

        /**
         * Node info.
         */
        public final NodeInfo nodeInfo;

        /**
         * Constructor.
         *
         * @param nodeInfo node info
         */
        public NodeInfoResp(final NodeInfo nodeInfo) {
            this.nodeInfo = nodeInfo;
        }
    }

}
