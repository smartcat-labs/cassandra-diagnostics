package io.smartcat.cassandra.diagnostics.module.requestrate;

import static io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateConfiguration.ALL_REQUESTS_TO_REPORT;
import static io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateConfiguration.REQUEST_META_DELIMITER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.Query.ConsistencyLevel;
import io.smartcat.cassandra.diagnostics.Query.StatementType;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.AtomicCounter;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Request rate module providing request rates at defined intervals. Request rates can be total or separate for read
 * and write.
 */
public class RequestRateModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(RequestRateModule.class);

    private static final String DEFAULT_MEASUREMENT_NAME = "request_rate";

    private static final String REQUEST_RATE_THREAD_NAME = "request-rate-timer";

    private final Map<String, AtomicCounter> requestRates;

    private final String service;

    private final int period;

    private final TimeUnit timeunit;

    private final long rateFactor;

    private final Timer timer;

    /**
     * Constructor.
     *
     * @param configuration        Module configuration
     * @param reporters            Reporter list
     * @param globalConfiguration  Global diagnostics configuration
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public RequestRateModule(ModuleConfiguration configuration, List<Reporter> reporters,
            final GlobalConfiguration globalConfiguration)
            throws ConfigurationException {
        super(configuration, reporters, globalConfiguration);

        RequestRateConfiguration config = RequestRateConfiguration.create(configuration.options);
        service = configuration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME);
        period = config.period();
        timeunit = config.timeunit();
        rateFactor = timeunit.toSeconds(period);
        requestRates = initRequestRates(config);

        logger.info("RequestRate module initialized with {} {} reporting period and requests to report: {}.", period,
                timeunit.name(), requestRates.keySet().toString());

        timer = new Timer(REQUEST_RATE_THREAD_NAME);
        timer.schedule(new RequestRateTask(), 0, config.reportingRateInMillis());
    }

    @Override
    public void process(Query query) {
        if (requestRates.containsKey(ALL_REQUESTS_TO_REPORT)) {
            requestRates.get(ALL_REQUESTS_TO_REPORT).increment();
        } else {
            String statementTypeConsistency = query.statementType().name() + REQUEST_META_DELIMITER
                    + query.consistencyLevel().name();
            if (requestRates.containsKey(statementTypeConsistency)) {
                requestRates.get(statementTypeConsistency).increment();
            }
        }
    }

    @Override
    public void stop() {
        logger.trace("Stopping request rate module.");
        timer.cancel();
    }

    private Map<String, AtomicCounter> initRequestRates(RequestRateConfiguration config) {
        Map<String, AtomicCounter> requestRates = new HashMap<>();

        if (config.requestsToReport().contains(ALL_REQUESTS_TO_REPORT)) {
            requestRates.put(ALL_REQUESTS_TO_REPORT, new AtomicCounter());
        } else {
            for (StatementType statementType : StatementType.values()) {
                for (ConsistencyLevel consistencyLevel : ConsistencyLevel.values()) {
                    String statementConsistencyPair = statementType + REQUEST_META_DELIMITER + consistencyLevel;
                    if (config.requestsToReport().contains(statementConsistencyPair)) {
                        requestRates.put(statementConsistencyPair, new AtomicCounter());
                    }
                }
            }
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
            for (String statementConsistency : requestRates.keySet()) {
                double requestRate = convertRate(requestRates.get(statementConsistency).sumThenReset());

                if (ALL_REQUESTS_TO_REPORT.equals(statementConsistency)) {
                    logger.info("Request rate: {}/{}", requestRate, timeunit.name());

                    report(createMeasurement(service, Query.StatementType.UNKNOWN.name(),
                            Query.ConsistencyLevel.UNKNOWN.name(),
                            requestRate));
                } else {
                    String[] requestMeta = statementConsistency.split(REQUEST_META_DELIMITER);
                    String statementType = requestMeta[0];
                    String consistencyLevel = requestMeta[1];
                    logger.info("{}-{} request rate: {}/{}", statementType, consistencyLevel, requestRate,
                            timeunit.name());

                    report(createMeasurement(service, statementType, consistencyLevel, requestRate));
                }
            }
        }
    }

    private Measurement createMeasurement(String service, String statementType, String consistencyLevel, double rate) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);
        tags.put("statementType", statementType);
        tags.put("consistencyLevel", consistencyLevel);
        return Measurement.create(service, rate, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags,
                new HashMap<String, String>());
    }
}
