package io.smartcat.cassandra.diagnostics.module.requestrate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.metrics.LongAdder;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Request rate module providing request rates at defined intervals. Request rates can be total or separate for read
 * and write.
 */
public class RequestRateModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(RequestRateModule.class);

    private static final String REQUEST_RATE_THREAD_NAME = "request-rate-module";

    private static final String PERIOD_PROP = "period";

    private static final String DEFAULT_PERIOD = "1";

    private static final String TIMEUNIT_PROP = "timeunit";

    private static final String DEFAULT_TIMEUNIT = "SECONDS";

    private static final String UPDATE_SUFFIX = "_update";

    private static final String SELECT_SUFFIX = "_select";

    private final LongAdder updateRequests;

    private final LongAdder selectRequests;

    private final String service;

    private final String updateService;

    private final String selectService;

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
        service = configuration.measurement;
        period = config.period();
        timeunit = config.timeunit();
        rateFactor = timeunit.toSeconds(1);

        logger.info("RequestRate module initialized with {} {} reporting period.", period, timeunit.name());
        updateService = service + UPDATE_SUFFIX;
        selectService = service + SELECT_SUFFIX;
        updateRequests = new LongAdder();
        selectRequests = new LongAdder();
        timer = new Timer(REQUEST_RATE_THREAD_NAME);
        timer.schedule(new RequestRateTask(), 0, config.reportingRateInMillis());
    }

    @Override
    protected boolean isForReporting(Query query) {
        return true;
    }

    @Override
    public Measurement transform(Query query) {
        if (query.statementType() == Query.StatementType.SELECT) {
            selectRequests.increment();
        } else if (query.statementType() == Query.StatementType.UPDATE) {
            updateRequests.increment();
        }

        return null;
    }

    @Override
    protected void report(Measurement measurement) {

    }

    @Override
    protected void stop() {
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
            double updateRate = convertRate(updateRequests.sumThenReset());
            double selectRate = convertRate(selectRequests.sumThenReset());

            logger.debug("Update request rate: {}/{}", updateRate, timeunit.name());
            logger.debug("Select request rate: {}/{}", selectRate, timeunit.name());

            Measurement updates = createMeasurement(updateService, updateRate);
            Measurement selects = createMeasurement(selectService, selectRate);

            for (Reporter reporter : reporters) {
                reporter.report(updates);
                reporter.report(selects);
            }
        }
    }

    private Measurement createMeasurement(String service, double rate) {
        return Measurement
                .create(service, rate, new Date().getTime(), TimeUnit.MILLISECONDS, new HashMap<String, String>(),
                        new HashMap<String, String>());
    }
}
