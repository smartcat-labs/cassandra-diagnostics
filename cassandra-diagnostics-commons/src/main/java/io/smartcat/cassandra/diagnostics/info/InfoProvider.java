package io.smartcat.cassandra.diagnostics.info;

import java.util.List;

/**
 * Info provider interface.
 */
public interface InfoProvider {

    /**
     * Gets the list of all active compactions.
     *
     * @return compaction info list
     */
    List<CompactionInfo> getCompactions();

    /**
     * Get the status of all thread pools.
     *
     * @return thread pools info list
     */
    List<TPStatsInfo> getTPStats();

    /**
     * Get the number of repair sessions.
     *
     * @return repair sessions
     */
    long getRepairSessions();

    /**
     * Get compaction settings info for specific node.
     *
     * @return compaction settings info
     */
    CompactionSettingsInfo getCompactionSettingsInfo();

    /**
     * Get the list of unreachable nodes.
     *
     * @return unreachable nodes list
     */
    List<String> getUnreachableNodes();

    /**
     * Get the information about node such as which protocols are active and uptime.
     *
     * @return NodeInfo for node
     */
    NodeInfo getNodeInfo();

}
