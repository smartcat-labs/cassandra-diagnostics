package io.smartcat.cassandra.diagnostics.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.tools.NodeProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.CompactionSettingsInfo;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
import io.smartcat.cassandra.diagnostics.info.NodeInfo;
import io.smartcat.cassandra.diagnostics.info.TPStatsInfo;

/**
 * NodeProbe class wrapper that exposes data and action functions.
 */
public class NodeProbeWrapper implements InfoProvider {

    private static final Logger logger = LoggerFactory.getLogger(NodeProbeWrapper.class);

    private final NodeProbe nodeProbe;

    private static final String REPAIR_THREAD_POOL_PREFIX = "Repair#";

    /**
     * NodeProbe constructor.
     *
     * @param host cassandra jmx host
     * @param port cassandra jmx port
     * @throws IOException JMX connection exception
     */
    public NodeProbeWrapper(String host, int port) throws IOException {
        this.nodeProbe = new NodeProbe(host, port);
    }

    /**
     * NodeProbe constructor.
     *
     * @param host cassandra jmx host
     * @param port cassandra jmx port
     * @param username cassandra jmx username (optional)
     * @param password cassandra jmx password (optional)
     * @throws IOException JMX connection exception
     */
    public NodeProbeWrapper(String host, int port, String username, String password) throws IOException {
        this.nodeProbe = new NodeProbe(host, port, username, password);
    }

    /**
     * Gets the list of all active compactions.
     *
     * @return compaction info list
     */
    public List<CompactionInfo> getCompactions() {
        List<CompactionInfo> compactions = new ArrayList<>();
        for (Map<String, String> compaction : this.nodeProbe.getCompactionManagerProxy().getCompactions()) {
            compactions.add(new CompactionInfo(Long.parseLong(compaction.get("total")),
                    Long.parseLong(compaction.get("completed")), compaction.get("unit"), compaction.get("taskType"),
                    compaction.get("keyspace"), compaction.get("columnfamily"), compaction.get("compactionId")));
        }
        return compactions;
    }

    /**
     * Get the status of all thread pools.
     *
     * @return thread pools info list
     */
    public List<TPStatsInfo> getTPStats() {
        List<TPStatsInfo> tpstats = new ArrayList<>();
        Multimap<String, String> threadPools = nodeProbe.getThreadPools();
        for (Map.Entry<String, String> tpool : threadPools.entries()) {
            tpstats.add(new TPStatsInfo(tpool.getValue(),
                    (int) nodeProbe.getThreadPoolMetric(tpool.getKey(), tpool.getValue(), "ActiveTasks"),
                    (long) nodeProbe.getThreadPoolMetric(tpool.getKey(), tpool.getValue(), "PendingTasks"),
                    (long) nodeProbe.getThreadPoolMetric(tpool.getKey(), tpool.getValue(), "CompletedTasks"),
                    (long) nodeProbe.getThreadPoolMetric(tpool.getKey(), tpool.getValue(), "CurrentlyBlockedTasks"),
                    (long) nodeProbe.getThreadPoolMetric(tpool.getKey(), tpool.getValue(), "TotalBlockedTasks")));
        }
        return tpstats;
    }

    /**
     * Gets number of repair sessions (repair on node has multiple repair sessions and by monitoring number of pending
     * repair sessions and number of active repair sessions we can monitor progress of repair).
     *
     * @return repair sessions info
     */
    public long getRepairSessions() {
        long repairSessions = 0;

        Multimap<String, String> threadPools = nodeProbe.getThreadPools();
        for (Map.Entry<String, String> tpool : threadPools.entries()) {
            if (tpool.getValue().startsWith(REPAIR_THREAD_POOL_PREFIX)) {
                int activeRepairSessions = (int) nodeProbe.getThreadPoolMetric(tpool.getKey(), tpool.getValue(),
                        "ActiveTasks");
                long pendingRepairSessions = (long) nodeProbe.getThreadPoolMetric(tpool.getKey(), tpool.getValue(),
                        "PendingTasks");
                repairSessions = activeRepairSessions + pendingRepairSessions;
            }
        }

        return repairSessions;
    }

    /**
     * Get unreachable nodes from the node's point of view (using the node's failure detection mechanism).
     *
     * @return unreachable nodes list. If no nodes are unreachable, returns the empty list.
     */
    @Override
    public List<String> getUnreachableNodes() {
        List<String> unreachableNodes = this.nodeProbe.getUnreachableNodes();
        return unreachableNodes;
    }

    /**
     * Get compaction settings info for.
     *
     * @return compaction settings info.
     */
    @Override
    public CompactionSettingsInfo getCompactionSettingsInfo() {
        return new CompactionSettingsInfo(nodeProbe.getCompactionThroughput(),
                nodeProbe.getCompactionManagerProxy().getCoreCompactorThreads(),
                nodeProbe.getCompactionManagerProxy().getMaximumCompactorThreads(),
                nodeProbe.getCompactionManagerProxy().getCoreValidationThreads(),
                nodeProbe.getCompactionManagerProxy().getMaximumValidatorThreads());
    }

    /**
     * Get the information if the native transport is active on the node.
     * Get the information about node such as which protocols are active and uptime.
     *
     * @return NodeInfo for the node
     */
    @Override
    public NodeInfo getNodeInfo() {
        NodeInfo nodeInfo = new NodeInfo(this.nodeProbe.isGossipRunning(), this.nodeProbe.isThriftServerRunning(),
                this.nodeProbe.isNativeTransportRunning(), this.nodeProbe.getUptime());
        return nodeInfo;
    }

}
