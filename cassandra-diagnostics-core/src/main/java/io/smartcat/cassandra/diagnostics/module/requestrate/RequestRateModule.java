package io.smartcat.cassandra.diagnostics.module.requestrate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
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

    private static final String REQUEST_RATE_THREAD_NAME = "request-rate-module";

    private static final String SUFFIX_SEPARATOR = "_";

    private final Map<StatementType, RequestRateCounter> requestRates;

    private final String service;

    private final int period;

    private final TimeUnit timeunit;

    private final long rateFactor;

    private final Timer timer;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public RequestRateModule(ModuleConfiguration configuration, List<Reporter> reporters)
            throws ConfigurationException {
        super(configuration, reporters);

        RequestRateConfiguration config = RequestRateConfiguration.create(configuration.options);
        service = configuration.getMeasurementOrDefault(DEFAULT_MEASUREMENT_NAME);
        period = config.period();
        timeunit = config.timeunit();
        rateFactor = timeunit.toSeconds(1);

        logger.info("RequestRate module initialized with {} {} reporting period.", period, timeunit.name());
        requestRates = new HashMap<>();
        for (StatementType statementType : StatementType.values()) {
            if (statementType != StatementType.UNKNOWN) {
                requestRates.put(statementType, new RequestRateCounter(
                        service.concat(SUFFIX_SEPARATOR).concat(statementType.name().toLowerCase()),
                        new AtomicCounter()));
            }
        }

        timer = new Timer(REQUEST_RATE_THREAD_NAME);
        timer.schedule(new RequestRateTask(), 0, config.reportingRateInMillis());
    }

    /**
     * Request rate counter wrapper class.
     */
    private class RequestRateCounter {

        public final String serviceName;

        public final AtomicCounter counter;

        RequestRateCounter(String serviceName, AtomicCounter counter) {
            this.serviceName = serviceName;
            this.counter = counter;
        }
    }

    @Override
    public void process(Query query) {
        requestRates.get(query.statementType()).counter.increment();
    }

    @Override
    public void stop() {
        logger.trace("Stopping request rate module.");
        timer.cancel();
    }

    private double convertRate(double rate) {
        return rate * rateFactor;
    }

    /**
     * Request rate reporter task that's executed at configured period.
     */
    private class RequestRateTask extends TimerTask {
        @Override
        public void run() {
            for (StatementType statementType : requestRates.keySet()) {
                RequestRateCounter requestRateCounter = requestRates.get(statementType);

                double requestRate = convertRate(requestRateCounter.counter.sumThenReset());

                logger.debug("{} request rate: {}/{}", statementType, requestRate, timeunit.name());

                Measurement measurement = createMeasurement(requestRateCounter.serviceName, requestRate);

                for (Reporter reporter : reporters) {
                    reporter.report(measurement);
                }
            }
        }
    }

    private Measurement createMeasurement(String service, double rate) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", hostname);
        return Measurement.create(service, rate, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags,
                new HashMap<String, String>());
    }
}
