package io.smartcat.cassandra.diagnostics.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.concurrent.JMXEnabledThreadPoolExecutorMBean;
import org.apache.cassandra.tools.NodeProbe;

import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
import io.smartcat.cassandra.diagnostics.info.TPStatsInfo;

/**
 * NodeProbe class wrapper that exposes data and action functions.
 */
public class NodeProbeWrapper implements InfoProvider {

    private final NodeProbe nodeProbe;

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
     * @param host     cassandra jmx host
     * @param port     cassandra jmx port
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

}
