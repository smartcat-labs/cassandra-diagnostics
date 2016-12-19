package io.smartcat.cassandra.diagnostics.module.slowquery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.AtomicCounter;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Slow query module providing reports of query execution times over a defined threshold.
 */
public class SlowQueryModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(SlowQueryModule.class);

    private static final String DEFAULT_MEASUREMENT_NAME = "slow_query";

    private static final String SLOW_QUERY_COUNT_SUFFIX = "_count";

    private final String service;

    private final String slowQueryCountMeasurementName;

    private final SlowQueryLogDecider slowQueryLogDecider;

    private final AtomicCounter slowQueryCount;

    private final Timer timer;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public SlowQueryModule(ModuleConfiguration configuration, List<Reporter> reporters) throws ConfigurationException {
        super(configuration, reporters);
        SlowQueryConfiguration config = SlowQueryConfiguration.create(configuration.options);

        service = configuration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME);
        slowQueryCountMeasurementName = service + SLOW_QUERY_COUNT_SUFFIX;

        slowQueryLogDecider = SlowQueryLogDecider.create(config);

        slowQueryCount = new AtomicCounter();
        if (config.slowQueryCountReportEnabled()) {
            timer = new Timer();
            timer.schedule(new SlowQueryReportTask(), 0, config.slowQueryCountReportingRateInMillis());
        } else {
            timer = null;
        }
    }

    @Override
    public void process(Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null");
        }

        if (!slowQueryLogDecider.isForReporting(query)) {
            return;
        }

        if (hostname == null) {
            logger.error("Cannot log slow query because hostname is not resolved");
            throw new IllegalArgumentException("Cannot log slow query because hostname is not resolved.");
        }

        slowQueryCount.increment();

        final Map<String, String> tags = new HashMap<>(4);
        tags.put("id", UUID.randomUUID().toString());
        tags.put("host", hostname);
        tags.put("statementType", query.statementType().toString());

        final Map<String, String> fields = new HashMap<>(4);
        fields.put("client", query.clientAddress());
        fields.put("statement", query.statement());

        final Measurement measurement = Measurement
                .create(service, query.executionTimeInMilliseconds(), query.startTimeInMilliseconds(),
                        TimeUnit.MILLISECONDS, tags, fields);

        logger.trace("Measurement transformed: {}", measurement);
        report(measurement);
    }

    @Override
    public void stop() {
        timer.cancel();
    }

    /**
     * Slow query count reporter task that's executed at configured period.
     */
    private class SlowQueryReportTask extends TimerTask {
        @Override
        public void run() {
            double count = slowQueryCount.sumThenReset();

            Measurement measurement = createSlowQueryCountMeasurement(count);

            for (Reporter reporter : reporters) {
                reporter.report(measurement);
            }
        }
    }

    private Measurement createSlowQueryCountMeasurement(double count) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", hostname);
        return Measurement
                .create(slowQueryCountMeasurementName, count, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags,
                        new HashMap<String, String>());
    }
}
