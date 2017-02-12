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
import io.smartcat.cassandra.diagnostics.info.CompactionInfo;
import io.smartcat.cassandra.diagnostics.info.InfoProvider;
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

    private final Timer timer;

    private final InfoProvider infoProvider;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     */
    public StatusModule(ModuleConfiguration configuration, List<Reporter> reporters) {
        super(configuration, reporters);

        infoProvider = DiagnosticsAgent.getInfoProvider();

        timer = new Timer(STATUS_THREAD_NAME);
        timer.scheduleAtFixedRate(new StatusTask(), 0, 60000);
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
            for (CompactionInfo compactionInfo : infoProvider.getCompactions()) {
                report(createMeasurement(compactionInfo));
            }
        }
    }

    private Measurement createMeasurement(CompactionInfo compactionInfo) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", hostname);

        final Map<String, String> fields = new HashMap<>();
        fields.put("compactionId", compactionInfo.id);
        fields.put("keyspace", compactionInfo.keyspace);
        fields.put("columnfamily", compactionInfo.columnFamily);
        fields.put("taskType", compactionInfo.taskType);
        fields.put("unit", compactionInfo.unit);
        fields.put("total", Long.toString(compactionInfo.total));
        fields.put("completed", Long.toString(compactionInfo.completed));
        fields.put("completedPercentage", Double.toString(compactionInfo.completedPercentage));

        return Measurement
                .create(DEFAULT_COMPACTION_INFO_MEASUREMENT_NAME, 0, System.currentTimeMillis(), TimeUnit.MILLISECONDS,
                        tags, fields);
    }

}
