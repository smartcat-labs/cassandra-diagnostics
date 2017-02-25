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
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
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

    private static final String DEFAULT_TPSTATS_INFO_MEASUREMENT_NAME = "tpstats_info";

    private static final String DEFAULT_REPAIR_SESSIONS_MEASUREMENT_NAME = "repair_sessions";

    private final int period;

    private final TimeUnit timeunit;

    private final boolean compactionsEnabled;

    private final boolean tpStatsEnabled;

    private final boolean repairsEnabled;

    private final Timer timer;

    private final InfoProvider infoProvider;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     * @throws ConfigurationException configuration parsing exception
     */
    public StatusModule(ModuleConfiguration configuration, List<Reporter> reporters) throws ConfigurationException {
        super(configuration, reporters);

        StatusConfiguration config = StatusConfiguration.create(configuration.options);
        period = config.period();
        timeunit = config.timeunit();
        compactionsEnabled = config.compactionsEnabled();
        tpStatsEnabled = config.tpStatsEnabled();
        repairsEnabled = config.repairsEnabled();

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
                report(createMeasurement(infoProvider.getRepairSessions()));
            }
        }
    }

    private Measurement createMeasurement(CompactionInfo compactionInfo) {
        final Map<String, String> tags = new HashMap<>(4);
        tags.put("host", hostname);
        tags.put("keyspace", compactionInfo.keyspace);
        tags.put("columnfamily", compactionInfo.columnFamily);
        tags.put("taskType", compactionInfo.taskType);

        final Map<String, String> fields = new HashMap<>();
        fields.put("compactionId", compactionInfo.compactionId);
        fields.put("unit", compactionInfo.unit);
        fields.put("total", Long.toString(compactionInfo.total));
        fields.put("completed", Long.toString(compactionInfo.completed));
        fields.put("completedPercentage", Double.toString(compactionInfo.completedPercentage));

        return Measurement.create(DEFAULT_COMPACTION_INFO_MEASUREMENT_NAME, null, System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, fields);
    }

    private Measurement createMeasurement(TPStatsInfo tpStatsInfo) {
        final Map<String, String> tags = new HashMap<>(2);
        tags.put("host", hostname);
        tags.put("threadPool", tpStatsInfo.threadPool);

        final Map<String, String> fields = new HashMap<>();
        fields.put("activeTasks", Long.toString(tpStatsInfo.activeTasks));
        fields.put("pendingTasks", Long.toString(tpStatsInfo.pendingTasks));
        fields.put("completedTasks", Long.toString(tpStatsInfo.completedTasks));
        fields.put("currentlyBlockedTasks", Long.toString(tpStatsInfo.currentlyBlockedTasks));
        fields.put("totalBlockedTasks", Long.toString(tpStatsInfo.totalBlockedTasks));

        return Measurement
                .create(DEFAULT_TPSTATS_INFO_MEASUREMENT_NAME, null, System.currentTimeMillis(), TimeUnit.MILLISECONDS,
                        tags, fields);
    }

    private Measurement createMeasurement(long repairSessions) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", hostname);

        final Map<String, String> fields = new HashMap<>();

        return Measurement
                .create(DEFAULT_REPAIR_SESSIONS_MEASUREMENT_NAME, (double) repairSessions, System.currentTimeMillis(),
                        TimeUnit.MILLISECONDS, tags, fields);
    }

}
