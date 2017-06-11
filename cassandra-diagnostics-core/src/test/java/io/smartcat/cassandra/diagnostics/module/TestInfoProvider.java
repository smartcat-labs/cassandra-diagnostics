package io.smartcat.cassandra.diagnostics.module;

import java.util.List;

import akka.actor.Props;
import akka.japi.Creator;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.CompactionSettingsInfo;
import io.smartcat.cassandra.diagnostics.info.InfoProviderActor;
import io.smartcat.cassandra.diagnostics.info.NodeInfo;
import io.smartcat.cassandra.diagnostics.info.TPStatsInfo;

/**
 * Test info provider.
 */
public class TestInfoProvider extends InfoProviderActor {

    public static Props props(final List<CompactionInfo> compactions, final List<TPStatsInfo> tpStats,
            final long repairSessions, final CompactionSettingsInfo compactionSettingsInfo,
            final List<String> unreachableNodes, final NodeInfo nodeInfo, final Configuration configuration)
            throws ClassNotFoundException, NoSuchMethodException {
        return Props.create(TestInfoProvider.class, new Creator<TestInfoProvider>() {
            @Override
            public TestInfoProvider create() throws Exception {
                return new TestInfoProvider(compactions, tpStats, repairSessions, compactionSettingsInfo,
                        unreachableNodes, nodeInfo, configuration);
            }
        });
    }

    public final List<CompactionInfo> compactions;
    public final List<TPStatsInfo> tpStats;
    public final long repairSessions;
    public final CompactionSettingsInfo compactionSettingsInfo;
    public final List<String> unreachableNodes;
    public final NodeInfo nodeInfo;
    public final Configuration configuration;

    public TestInfoProvider(final List<CompactionInfo> compactions, final List<TPStatsInfo> tpStats,
            final long repairSessions, final CompactionSettingsInfo compactionSettingsInfo,
            final List<String> unreachableNodes, final NodeInfo nodeInfo, final Configuration configuration) {
        super(configuration.connector);

        this.compactions = compactions;
        this.tpStats = tpStats;
        this.repairSessions = repairSessions;
        this.compactionSettingsInfo = compactionSettingsInfo;
        this.unreachableNodes = unreachableNodes;
        this.nodeInfo = nodeInfo;
        this.configuration = configuration;
    }

    @Override
    protected List<CompactionInfo> getCompactions() {
        return compactions;
    }

    @Override
    protected List<TPStatsInfo> getTPStats() {
        return tpStats;
    }

    @Override
    protected long getRepairSessions() {
        return repairSessions;
    }

    @Override
    protected CompactionSettingsInfo getCompactionSettingsInfo() {
        return compactionSettingsInfo;
    }

    @Override
    protected List<String> getUnreachableNodes() {
        return unreachableNodes;
    }

    @Override
    protected NodeInfo getNodeInfo() {
        return nodeInfo;
    }

}
