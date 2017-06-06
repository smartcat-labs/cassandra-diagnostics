package io.smartcat.cassandra.diagnostics.module.slowquery;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.Query.StatementType;
import io.smartcat.cassandra.diagnostics.actor.ModuleActor;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.AtomicCounter;

/**
 * Slow query module providing reports of query execution times over a defined threshold.
 */
public class SlowQueryModule extends ModuleActor {

    private static final String DEFAULT_MEASUREMENT_NAME = "slow_query";

    private static final String SLOW_QUERY_COUNT_SUFFIX = "_count";

    private static final String SLOW_QUERY_COUNT_THREAD_NAME = "slow-query-count-timer";

    private final SlowQueryConfiguration config;

    private final String service;

    private final String slowQueryCountMeasurementName;

    private final SlowQueryLogDecider slowQueryLogDecider;

    private final Map<StatementType, AtomicCounter> slowQueryCounts;

    private Timer timer;

    /**
     * Constructor.
     *
     * @param moduleName    Module class name
     * @param configuration configuration
     * @throws ConfigurationException configuration parsing exception
     */
    public SlowQueryModule(final String moduleName, final Configuration configuration) throws ConfigurationException {
        super(moduleName, configuration);

        config = SlowQueryConfiguration.create(moduleConfiguration.options);
        service = moduleConfiguration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME);
        slowQueryCountMeasurementName = service + SLOW_QUERY_COUNT_SUFFIX;

        slowQueryLogDecider = SlowQueryLogDecider.create(config);
        slowQueryCounts = new HashMap<>();

        for (StatementType statementType : StatementType.values()) {
            slowQueryCounts.put(statementType, new AtomicCounter());
        }
    }

    @Override
    protected void start() {
        if (config.slowQueryCountReportEnabled()) {
            timer = new Timer(SLOW_QUERY_COUNT_THREAD_NAME);
            timer.schedule(new SlowQueryReportTask(), 0, config.slowQueryCountReportingRateInMillis());
        } else {
            timer = null;
        }
    }

    @Override
    public void stop() {
        logger.debug("Stopping slow query module.");
        if (timer != null) {
            timer.cancel();
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

        if (configuration.global.hostname == null) {
            logger.error("Cannot log slow query because hostname is not resolved");
            throw new IllegalArgumentException("Cannot log slow query because hostname is not resolved.");
        }

        if (config.slowQueryCountReportEnabled()) {
            slowQueryCounts.get(query.statementType()).increment();
        }

        if (config.slowQueryReportEnabled()) {
            final Map<String, String> tags = new HashMap<>(4);
            tags.put("host", configuration.global.hostname);
            tags.put("systemName", configuration.global.systemName);
            tags.put("statementType", query.statementType().toString());

            final Map<String, String> fields = new HashMap<>(4);
            fields.put("client", query.clientAddress());
            fields.put("statement", query.statement());
            fields.put("consistencyLevel", query.consistencyLevel().name());

            final Measurement measurement = Measurement.createSimple(service,
                    (double) query.executionTimeInMilliseconds(), query.startTimeInMilliseconds(),
                    TimeUnit.MILLISECONDS, tags, fields);

            logger.debug("Measurement transformed: {}", measurement);
            report(measurement);
        }
    }

    /**
     * Slow query count reporter task that's executed at configured period.
     */
    private class SlowQueryReportTask extends TimerTask {
        @Override
        public void run() {
            for (StatementType statementType : slowQueryCounts.keySet()) {
                double count = slowQueryCounts.get(statementType).sumThenReset();

                report(createSlowQueryCountMeasurement(count, statementType));
            }
        }
    }

    private Measurement createSlowQueryCountMeasurement(double count, StatementType statementType) {
        final Map<String, String> tags = new HashMap<>(2);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);
        tags.put("statementType", statementType.toString());
        return Measurement.createSimple(slowQueryCountMeasurementName, count, System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, new HashMap<String, String>());
    }
}
