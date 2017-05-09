package io.smartcat.cassandra.diagnostics.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.concurrent.JMXEnabledThreadPoolExecutorMBean;
import org.apache.cassandra.tools.NodeProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.CompactionSettingsInfo;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
import io.smartcat.cassandra.diagnostics.info.TPStatsInfo;

/**
 * NodeProbe class wrapper that exposes data and action functions.
 */
public class NodeProbeWrapper implements InfoProvider {

    private static final Logger logger = LoggerFactory.getLogger(InfoProvider.class);

    private final NodeProbe nodeProbe;

    private static final String REPAIR_THREAD_POOL_NAME = "AntiEntropySessions";

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
                    compaction.get("keyspace"), compaction.get("columnfamily"), null));
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
        Iterator<Map.Entry<String, JMXEnabledThreadPoolExecutorMBean>> threads = nodeProbe.getThreadPoolMBeanProxies();
        while (threads.hasNext()) {
            Map.Entry<String, JMXEnabledThreadPoolExecutorMBean> thread = threads.next();
            JMXEnabledThreadPoolExecutorMBean threadPoolProxy = thread.getValue();
            tpstats.add(new TPStatsInfo(thread.getKey(), threadPoolProxy.getActiveCount(),
                    threadPoolProxy.getPendingTasks(), threadPoolProxy.getCompletedTasks(),
                    threadPoolProxy.getCurrentlyBlockedTasks(), threadPoolProxy.getTotalBlockedTasks()));
        }
        return tpstats;
    }

    /**
     * Gets number of repair sessions pending (repair on node has multiple repair sessions and by monitoring number of
     * pending repair sessions and active repair sessions we can monitor progress of repair).
     *
     * @return repair session info
     */
    public long getRepairSessions() {
        long repairSessions = 0;

        Iterator<Map.Entry<String, JMXEnabledThreadPoolExecutorMBean>> threads = nodeProbe.getThreadPoolMBeanProxies();
        while (threads.hasNext()) {
            Map.Entry<String, JMXEnabledThreadPoolExecutorMBean> thread = threads.next();

            if (thread.getKey().equals(REPAIR_THREAD_POOL_NAME)) {
                JMXEnabledThreadPoolExecutorMBean threadPoolProxy = thread.getValue();
                repairSessions = threadPoolProxy.getPendingTasks() + threadPoolProxy.getActiveCount();
            }
        }

        return repairSessions;
    }

    /**
     * Get unreachable nodes from the node's point of view (using the node's failure detection mechanism).
     *
     * @return unreachable nodes list
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
}
