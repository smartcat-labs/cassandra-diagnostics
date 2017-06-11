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
        public final ActorRef infoProvider;

        public InfoProviderRef(final ActorRef infoProvider) {
            this.infoProvider = infoProvider;
        }
    }

    /**
     * Unreachable nodes query response.
     */
    public static final class UnreachableNodesResp extends QueryResponse {
        public final List<String> unreachableNodes;

        public UnreachableNodesResp(final List<String> unreachableNodes) {
            this.unreachableNodes = unreachableNodes;
        }
    }

    /**
     * Compactions info query response.
     */
    public static final class CompactionsResp extends QueryResponse {
        public final CompactionSettingsInfo compactionSettingsInfo;
        public final List<CompactionInfo> compactionInfo;

        public CompactionsResp(final CompactionSettingsInfo compactionSettingsInfo, final List<CompactionInfo> compactionInfo) {
            this.compactionInfo = compactionInfo;
            this.compactionSettingsInfo = compactionSettingsInfo;
        }
    }

    /**
     * Thread pool stats query response.
     */
    public static final class TPStatsResp extends QueryResponse {
        public final List<TPStatsInfo> tpStatsInfo;

        public TPStatsResp(final List<TPStatsInfo> tpStatsInfo) {
            this.tpStatsInfo = tpStatsInfo;
        }
    }

    /**
     * Repair sessions query response.
     */
    public static final class RepairSessionsResp extends QueryResponse {
        public final long repairSessions;

        public RepairSessionsResp(final long repairSessions) {
            this.repairSessions = repairSessions;
        }
    }

    /**
     * Node info query response.
     */
    public static final class NodeInfoResp extends QueryResponse {
        public final NodeInfo nodeInfo;

        public NodeInfoResp(final NodeInfo nodeInfo) {
            this.nodeInfo = nodeInfo;
        }
    }

}
