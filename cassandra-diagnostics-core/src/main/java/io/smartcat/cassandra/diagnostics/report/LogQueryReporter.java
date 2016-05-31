package io.smartcat.cassandra.diagnostics.report;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.Reporter;
import io.smartcat.cassandra.diagnostics.ReporterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SLF4J based {@link Reporter} implementation. This reporter is using {@link Logger} to print query reports to a log
 * at {@code INFO} level.
 */
public class LogQueryReporter implements Reporter {

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
     * @param config configuration
     */
    public LogQueryReporter(ReporterConfiguration config) {

    }

    @Override
    public void report(Query query) {
        logger.info(LOG_TEMPLATE, query.getStartTimeInMilliseconds(), query.getExecutionTimeInMilliseconds(),
                query.getClientAddress(), query.getStatement());
    }

}
