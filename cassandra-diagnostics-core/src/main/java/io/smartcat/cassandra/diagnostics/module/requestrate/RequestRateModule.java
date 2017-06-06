package io.smartcat.cassandra.diagnostics.module.requestrate;

import static io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateConfiguration.ALL_CONSISTENCY_LEVELS;
import static io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateConfiguration.ALL_STATEMENT_TYPES;
import static io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateConfiguration.REQUEST_META_DELIMITER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.actor.ModuleActor;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.AtomicCounter;

/**
 * Request rate module providing request rates at defined intervals. Request rates can be total or separate for read
 * and write.
 */
public class RequestRateModule extends ModuleActor {

    private static final String DEFAULT_MEASUREMENT_NAME = "request_rate";

    private static final String REQUEST_RATE_THREAD_NAME = "request-rate-timer";

    private final RequestRateConfiguration config;

    private final Set<RequestRate> requestRates;

    private final String service;

    private final long rateFactor;

    private Timer timer;

    /**
     * Constructor.
     *
     * @param moduleName    Module class name
     * @param configuration configuration
     * @throws ConfigurationException configuration parsing exception
     */
    public RequestRateModule(final String moduleName, final Configuration configuration) throws ConfigurationException {
        super(moduleName, configuration);

        config = RequestRateConfiguration.create(moduleConfiguration.options);
        service = moduleConfiguration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME);
        rateFactor = config.timeunit().toSeconds(config.period());
        requestRates = initRequestRates(config);

        logger.info("RequestRate module initialized with {} {} reporting period and requests to report: {}.", config.period(),
                config.timeunit().name(), config.requestsToReport());
    }

    @Override
    protected void start() {
        timer = new Timer(REQUEST_RATE_THREAD_NAME);
        timer.schedule(new RequestRateTask(), 0, config.reportingRateInMillis());
    }

    @Override
    public void stop() {
        logger.debug("Stopping request rate module.");
        timer.cancel();
    }

    @Override
    public void process(Query query) {
        final String statementType = query.statementType().name();
        final String consistencyLevel = query.consistencyLevel().name();

        for (RequestRate requestRate : requestRates) {
            if (statementMatches(statementType, requestRate)
                    && consistencyLevelMatches(consistencyLevel, requestRate)) {
                requestRate.increment();
            }
        }
    }

    /**
     * Request rate class.
     */
    private class RequestRate {
        public final String statementType;
        public final String consistencyLevel;
        public final AtomicCounter counter = new AtomicCounter();

        /**
         * Constructor.
         *
         * @param requestPattern Configured request rate report pattern
         */
        public RequestRate(String requestPattern) {
            String[] requestMeta = requestPattern.split(REQUEST_META_DELIMITER);
            this.statementType = requestMeta[0];
            this.consistencyLevel = requestMeta[1];
        }

        public void increment() {
            counter.increment();
        }

        public long sumThenReset() {
            return counter.sumThenReset();
        }
    }

    private boolean consistencyLevelMatches(final String consistencyLevel, RequestRate requestRate) {
        return requestRate.consistencyLevel.equals(ALL_CONSISTENCY_LEVELS)
                || requestRate.consistencyLevel.equals(consistencyLevel);
    }

    private boolean statementMatches(final String statementType, RequestRate requestRate) {
        return requestRate.statementType.equals(ALL_STATEMENT_TYPES)
                || requestRate.statementType.equals(statementType);
    }

    private Set<RequestRate> initRequestRates(RequestRateConfiguration config) {
        final Set<RequestRate> requestRates = new HashSet<>();

        for (String requestToReport : config.requestsToReport()) {
            requestRates.add(new RequestRate(requestToReport));
        }

        return requestRates;
    }

    private double convertRate(double rate) {
        return rate / rateFactor;
    }

    /**
     * Request rate reporter task that's executed at configured period.
     */
    private class RequestRateTask extends TimerTask {
        @Override
        public void run() {
            for (RequestRate requestRate : requestRates) {
                double rate = convertRate(requestRate.sumThenReset());
                report(createMeasurement(service, requestRate.statementType, requestRate.consistencyLevel, rate));
            }
        }
    }

    private Measurement createMeasurement(String service, String statementType, String consistencyLevel, double rate) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", configuration.global.hostname);
        tags.put("systemName", configuration.global.systemName);
        tags.put("statementType", statementType);
        tags.put("consistencyLevel", consistencyLevel);
        return Measurement.createSimple(service, rate, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags,
                new HashMap<>());
    }
}
