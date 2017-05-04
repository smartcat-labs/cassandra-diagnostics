package io.smartcat.cassandra.diagnostics.module.requestrate;

import static io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateConfiguration.REQUEST_META_DELIMITER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
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

    private final Set<RequestRate> requestRates;

    private final String service;

    private final int period;

    private final TimeUnit timeunit;

    private final long rateFactor;

    private final Timer timer;

    /**
     * Constructor.
     *
     * @param configuration       Module configuration
     * @param reporters           Reporter list
     * @param globalConfiguration Global diagnostics configuration
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public RequestRateModule(ModuleConfiguration configuration, List<Reporter> reporters,
            final GlobalConfiguration globalConfiguration) throws ConfigurationException {
        super(configuration, reporters, globalConfiguration);

        RequestRateConfiguration config = RequestRateConfiguration.create(configuration.options);
        service = configuration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME);
        period = config.period();
        timeunit = config.timeunit();
        rateFactor = timeunit.toSeconds(period);
        requestRates = initRequestRates(config);

        logger.info("RequestRate module initialized with {} {} reporting period and requests to report: {}.", period,
                timeunit.name(), config.requestsToReport());

        timer = new Timer(REQUEST_RATE_THREAD_NAME);
        timer.schedule(new RequestRateTask(), 0, config.reportingRateInMillis());
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

    @Override
    public void process(Query query) {
        final String statementType = query.statementType().name();
        final String consistencyLevel = query.consistencyLevel().name();

        for (RequestRate requestRate : requestRates) {
            if ((requestRate.statementType.equals("*") || requestRate.statementType.equals(statementType)) && (
                    requestRate.consistencyLevel.equals("*") || requestRate.consistencyLevel
                            .equals(consistencyLevel))) {
                requestRate.increment();
            }
        }
    }

    @Override
    public void stop() {
        logger.trace("Stopping request rate module.");
        timer.cancel();
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
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);
        tags.put("statementType", statementType);
        tags.put("consistencyLevel", consistencyLevel);
        return Measurement.create(service, rate, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags,
                new HashMap<String, String>());
    }
}
