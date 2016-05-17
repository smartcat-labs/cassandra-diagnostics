package io.smartcat.cassandra.diagnostics.report;

import io.smartcat.cassandra.diagnostics.config.ReporterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SLF4J based {@link QueryReporter} implementation. This reporter is using {@link Logger} to print query reports to a
 * log at {@code INFO} level.
 */
public class LogQueryReporter implements QueryReporter {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(LogQueryReporter.class);

    /**
     * String template for logging query report.
     */
    private static final String LOG_TEMPLATE =
            "QueryReport [startTimeInMilliseconds={}, executionTimeInMilliseconds={}, clientAddress={}, statement={}]";

    /**
     * Constructor.
     *
     * @param config configuration
     */
    public LogQueryReporter(ReporterConfiguration config) {

    }

    @Override
    public void report(QueryReport queryReport) {
        logger.info(LOG_TEMPLATE, queryReport.startTimeInMilliseconds, queryReport.executionTimeInMilliseconds,
                queryReport.clientAddress, queryReport.statement);
    }

}
