package io.smartcat.cassandra.diagnostics.module.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.DiagnosticsAgent;
import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.CompactionSettingsInfo;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
import io.smartcat.cassandra.diagnostics.info.NodeInfo;
import io.smartcat.cassandra.diagnostics.info.TPStatsInfo;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Status module collecting node status information.
 */
public class StatusModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(StatusModule.class);

    private static final String STATUS_THREAD_NAME = "status-module";

    private static final String DEFAULT_COMPACTION_INFO_MEASUREMENT_NAME = "compaction_info";

    private static final String DEFAULT_COMPACTION_SETTINGS_INFO_MEASUREMENT_NAME = "compaction_settings_info";

    private static final String DEFAULT_REPAIR_SESSIONS_MEASUREMENT_NAME = "repair_sessions";

    private static final String DEFAULT_NODE_INFO_MEASUREMENT_NAME = "node_info";

    private final int period;

    private final TimeUnit timeunit;

    private final boolean compactionsEnabled;

    private final boolean tpStatsEnabled;

    private final boolean repairsEnabled;

    private final boolean nodeInfoEnabled;

    private final Timer timer;

    private final InfoProvider infoProvider;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters Reporter list
     * @param globalConfiguration Global diagnostics configuration
     * @throws ConfigurationException configuration parsing exception
     */
    public StatusModule(ModuleConfiguration configuration, List<Reporter> reporters,
            final GlobalConfiguration globalConfiguration) throws ConfigurationException {
        super(configuration, reporters, globalConfiguration);

        StatusConfiguration config = StatusConfiguration.create(configuration.options);
        period = config.period();
        timeunit = config.timeunit();
        compactionsEnabled = config.compactionsEnabled();
        tpStatsEnabled = config.tpStatsEnabled();
        repairsEnabled = config.repairsEnabled();
        nodeInfoEnabled = config.nodeInfoEnabled();

        infoProvider = DiagnosticsAgent.getInfoProvider();
        if (infoProvider == null) {
            logger.warn("Failed to initialize StatusModule. Info provider is null");
            timer = null;
        } else {
            timer = new Timer(STATUS_THREAD_NAME);
            timer.scheduleAtFixedRate(new StatusTask(), 0, config.reportingRateInMillis());
        }
    }

    @Override
    public void stop() {
        logger.trace("Stopping status module.");
        timer.cancel();
    }

    /**
     * Status collector task that's executed at configured period.
     */
    private class StatusTask extends TimerTask {
        @Override
        public void run() {
            if (compactionsEnabled) {
                report(createMeasurement(infoProvider.getCompactionSettingsInfo()));
                for (CompactionInfo compactionInfo : infoProvider.getCompactions()) {
                    report(createMeasurement(compactionInfo));
                }
            }
            if (tpStatsEnabled) {
                for (TPStatsInfo tpStatsInfo : infoProvider.getTPStats()) {
                    report(createMeasurement(tpStatsInfo));
                }
            }
            if (repairsEnabled) {
                report(createSimpleMeasurement(DEFAULT_REPAIR_SESSIONS_MEASUREMENT_NAME,
                        (double) infoProvider.getRepairSessions()));
            }
            if (nodeInfoEnabled) {
                NodeInfo nodeInfo = infoProvider.getNodeInfo();
                report(createMeasurement(nodeInfo));
            }
        }
    }

    private Measurement createMeasurement(CompactionSettingsInfo compactionSettingsInfo) {
        final Map<String, String> tags = new HashMap<>(4);
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("compactionThroughput", Integer.toString(compactionSettingsInfo.compactionThroughput));
        fields.put("coreCompactorThreads", Integer.toString(compactionSettingsInfo.coreCompactorThreads));
        fields.put("maximumCompactorThreads", Integer.toString(compactionSettingsInfo.maximumCompactorThreads));
        fields.put("coreValidatorThreads", Integer.toString(compactionSettingsInfo.coreValidatorThreads));
        fields.put("maximumValidatorThreads", Integer.toString(compactionSettingsInfo.maximumValidatorThreads));

        return Measurement.create(DEFAULT_COMPACTION_SETTINGS_INFO_MEASUREMENT_NAME, null, System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, fields);
    }

    private Measurement createMeasurement(CompactionInfo compactionInfo) {
        final Map<String, String> tags = new HashMap<>(4);
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);
        tags.put("keyspace", compactionInfo.keyspace);
        tags.put("columnfamily", compactionInfo.columnFamily);
        tags.put("taskType", compactionInfo.taskType);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("compactionId", compactionInfo.compactionId);
        fields.put("unit", compactionInfo.unit);
        fields.put("total", Long.toString(compactionInfo.total));
        fields.put("completed", Long.toString(compactionInfo.completed));
        fields.put("completedPercentage", Double.toString(compactionInfo.completedPercentage));

        return Measurement.create(DEFAULT_COMPACTION_INFO_MEASUREMENT_NAME, null, System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, fields);
    }

    private Measurement createMeasurement(TPStatsInfo tpStatsInfo) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("activeTasks", Long.toString(tpStatsInfo.activeTasks));
        fields.put("pendingTasks", Long.toString(tpStatsInfo.pendingTasks));
        fields.put("completedTasks", Long.toString(tpStatsInfo.completedTasks));
        fields.put("currentlyBlockedTasks", Long.toString(tpStatsInfo.currentlyBlockedTasks));
        fields.put("totalBlockedTasks", Long.toString(tpStatsInfo.totalBlockedTasks));

        return Measurement.create(tpStatsInfo.threadPool, null, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags,
                fields);
    }

    private Measurement createSimpleMeasurement(String name, double value) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);

        final Map<String, String> fields = new HashMap<>();

        return Measurement.create(name, value, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);
    }

    private Measurement createMeasurement(NodeInfo nodeInfo) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("gossipActive", Boolean.toString(nodeInfo.gossipActive));
        fields.put("thriftActive", Boolean.toString(nodeInfo.thriftActive));
        fields.put("nativeTransportActive", Boolean.toString(nodeInfo.nativeTransportActive));
        fields.put("uptimeInSeconds", Long.toString(nodeInfo.uptimeInSeconds));

        return Measurement.create(DEFAULT_NODE_INFO_MEASUREMENT_NAME, null, System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, fields);
    }

}
