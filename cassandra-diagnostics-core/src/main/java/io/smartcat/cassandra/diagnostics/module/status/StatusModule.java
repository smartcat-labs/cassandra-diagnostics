package io.smartcat.cassandra.diagnostics.module.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.smartcat.cassandra.diagnostics.DiagnosticsAgent;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.actor.ModuleActor;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.CompactionSettingsInfo;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
import io.smartcat.cassandra.diagnostics.info.NodeInfo;
import io.smartcat.cassandra.diagnostics.info.TPStatsInfo;

/**
 * Status module collecting node status information.
 */
public class StatusModule extends ModuleActor {

    private static final String STATUS_THREAD_NAME = "status-module";

    private static final String DEFAULT_COMPACTION_INFO_MEASUREMENT_NAME = "compaction_info";

    private static final String DEFAULT_COMPACTION_SETTINGS_INFO_MEASUREMENT_NAME = "compaction_settings_info";

    private static final String DEFAULT_REPAIR_SESSIONS_MEASUREMENT_NAME = "repair_sessions";

    private static final String DEFAULT_NODE_INFO_MEASUREMENT_NAME = "node_info";

    private StatusConfiguration config;

    private InfoProvider infoProvider;

    private Timer timer;

    /**
     * Constructor.
     *
     * @param moduleName    Module class name
     * @param configuration configuration
     * @throws ConfigurationException configuration parsing exception
     */
    public StatusModule(final String moduleName, final Configuration configuration) throws ConfigurationException {
        super(moduleName, configuration);

        config = StatusConfiguration.create(moduleConfiguration.options);
        infoProvider = DiagnosticsAgent.getInfoProvider();
    }

    @Override
    protected void start() {
        if (infoProvider == null) {
            logger.warning("Failed to initialize StatusModule. Info provider is null");
            timer = null;
        } else {
            timer = new Timer(STATUS_THREAD_NAME);
            timer.scheduleAtFixedRate(new StatusTask(), 0, config.reportingRateInMillis());
        }
    }

    @Override
    protected void stop() {
        logger.debug("Stopping status module.");
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Status collector task that's executed at configured period.
     */
    private class StatusTask extends TimerTask {
        @Override
        public void run() {
            if (config.compactionsEnabled()) {
                report(createMeasurement(infoProvider.getCompactionSettingsInfo()));
                for (CompactionInfo compactionInfo : infoProvider.getCompactions()) {
                    report(createMeasurement(compactionInfo));
                }
            }
            if (config.tpStatsEnabled()) {
                for (TPStatsInfo tpStatsInfo : infoProvider.getTPStats()) {
                    report(createMeasurement(tpStatsInfo));
                }
            }
            if (config.repairsEnabled()) {
                report(createSimpleMeasurement(DEFAULT_REPAIR_SESSIONS_MEASUREMENT_NAME,
                        (double) infoProvider.getRepairSessions()));
            }
            if (config.nodeInfoEnabled()) {
                NodeInfo nodeInfo = infoProvider.getNodeInfo();
                report(createMeasurement(nodeInfo));
            }
        }
    }

    private Measurement createMeasurement(CompactionSettingsInfo compactionSettingsInfo) {
        final Map<String, String> tags = new HashMap<>(4);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("compactionThroughput", Integer.toString(compactionSettingsInfo.compactionThroughput));
        fields.put("coreCompactorThreads", Integer.toString(compactionSettingsInfo.coreCompactorThreads));
        fields.put("maximumCompactorThreads", Integer.toString(compactionSettingsInfo.maximumCompactorThreads));
        fields.put("coreValidatorThreads", Integer.toString(compactionSettingsInfo.coreValidatorThreads));
        fields.put("maximumValidatorThreads", Integer.toString(compactionSettingsInfo.maximumValidatorThreads));

        return Measurement.createComplex(DEFAULT_COMPACTION_SETTINGS_INFO_MEASUREMENT_NAME, System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, fields);
    }

    private Measurement createMeasurement(CompactionInfo compactionInfo) {
        final Map<String, String> tags = new HashMap<>(4);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);
        tags.put("keyspace", compactionInfo.keyspace);
        tags.put("columnfamily", compactionInfo.columnFamily);
        tags.put("taskType", compactionInfo.taskType);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("compactionId", compactionInfo.compactionId);
        fields.put("unit", compactionInfo.unit);
        fields.put("total", Long.toString(compactionInfo.total));
        fields.put("completed", Long.toString(compactionInfo.completed));
        fields.put("completedPercentage", Double.toString(compactionInfo.completedPercentage));

        return Measurement.createComplex(DEFAULT_COMPACTION_INFO_MEASUREMENT_NAME, System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, fields);
    }

    private Measurement createMeasurement(TPStatsInfo tpStatsInfo) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("activeTasks", Long.toString(tpStatsInfo.activeTasks));
        fields.put("pendingTasks", Long.toString(tpStatsInfo.pendingTasks));
        fields.put("completedTasks", Long.toString(tpStatsInfo.completedTasks));
        fields.put("currentlyBlockedTasks", Long.toString(tpStatsInfo.currentlyBlockedTasks));
        fields.put("totalBlockedTasks", Long.toString(tpStatsInfo.totalBlockedTasks));

        return Measurement
                .createComplex(tpStatsInfo.threadPool, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);
    }

    private Measurement createSimpleMeasurement(String name, double value) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        final Map<String, String> fields = new HashMap<>();

        return Measurement.createSimple(name, value, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);
    }

    private Measurement createMeasurement(NodeInfo nodeInfo) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);

        final Map<String, String> fields = new HashMap<>(5);
        fields.put("gossipActive", Integer.toString(nodeInfo.isGossipActive()));
        fields.put("thriftActive", Integer.toString(nodeInfo.isThriftActive()));
        fields.put("nativeTransportActive", Integer.toString(nodeInfo.isNativeTransportActive()));
        fields.put("uptimeInSeconds", Long.toString(nodeInfo.uptimeInSeconds));

        return Measurement
                .createComplex(DEFAULT_NODE_INFO_MEASUREMENT_NAME, System.currentTimeMillis(), TimeUnit.MILLISECONDS,
                        tags, fields);
    }

}
