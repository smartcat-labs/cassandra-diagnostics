package io.smartcat.cassandra.diagnostics.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

/**
 * A SLF4J based {@link Reporter} implementation. This reporter is using {@link Logger} to print query reports to a log
 * at {@code INFO} level.
 */
public class LogQueryReporter extends Reporter {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(LogQueryReporter.class);

    /**
     * String template for logging query report.
     */
    private static final String LOG_TEMPLATE =
            "Query [startTimeInMilliseconds={}, executionTimeInMilliseconds={}, " + "clientAddress={}, statement={}]";

    /**
     * Constructor.
     *
     * @param configuration configuration
     */
    public LogQueryReporter(ReporterConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void report(Measurement measurement) {
        Query query = measurement.query();
        logger.info(LOG_TEMPLATE, query.startTimeInMilliseconds(), query.executionTimeInMilliseconds(),
                query.clientAddress(), query.statement());
    }

}
