package io.smartcat.cassandra.diagnostics;

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
    public void report(Query queryReport) {
        logger.info(LOG_TEMPLATE, queryReport.startTimeInMilliseconds, queryReport.executionTimeInMilliseconds,
                queryReport.clientAddress, queryReport.statement);
    }

}
