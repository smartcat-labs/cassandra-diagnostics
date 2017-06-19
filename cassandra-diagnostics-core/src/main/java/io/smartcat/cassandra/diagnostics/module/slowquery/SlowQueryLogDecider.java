package io.smartcat.cassandra.diagnostics.module.slowquery;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.query.Query;

/**
 * Decider which will decide if query should be reported based on configuration.
 *
 */
public class SlowQueryLogDecider {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(SlowQueryModule.class);

    private final SlowQueryConfiguration slowQueryConfiguration;

    private SlowQueryLogDecider(SlowQueryConfiguration slowQueryConfiguration) {
        this.slowQueryConfiguration = slowQueryConfiguration;
    }

    /**
     * Create SlowQueryLogDecider for provided configuration.
     * @param configuration SlowQueryConfiguration which is active.
     * @return slow query decider for this configuration.
     */
    public static SlowQueryLogDecider create(SlowQueryConfiguration configuration) {
        return new SlowQueryLogDecider(configuration);
    }

    /**
     * Based on defined criteria decide if this query is eligible for reporting.
     * @param query Query candidate for report.
     * @return if this query is eligible for report.
     */
    public boolean isForReporting(Query query) {
        if (executionTimeForLogging(query.executionTimeInMilliseconds()) && tableForLogging(query)
                && typeForLogging(query)) {
            return true;
        }

        return false;
    }

    private boolean executionTimeForLogging(long executionTimeInMilliseconds) {
        if (slowQueryConfiguration.slowQueryThreshold() == 0) {
            logger.trace("Slow query threshold turned off, logging all queries.");
            return true;
        }

        logger.trace("Checking if execution time:{} is above threshold: {}", executionTimeInMilliseconds,
                slowQueryConfiguration.slowQueryThreshold());
        if (executionTimeInMilliseconds > slowQueryConfiguration.slowQueryThreshold()) {
            return true;
        }

        return false;
    }

    private boolean typeForLogging(Query query) {
        logger.trace("Checking if query type is for logging.");
        boolean logAll = slowQueryConfiguration.queryTypesToLog().contains("ALL");
        boolean queryTypeMatches = slowQueryConfiguration.queryTypesToLog().contains(query.statementType().toString());
        return logAll || queryTypeMatches;
    }

    private boolean tableForLogging(Query query) {
        logger.trace("Checking if table is in tables for logging.");
        if (slowQueryConfiguration.tablesForLogging().isEmpty()) {
            return true;
        }

        if (StringUtils.isBlank(query.fullTableName())) {
            logger.debug("Query does not have table name.");
            return false;
        }

        for (String tableForLogging : slowQueryConfiguration.tablesForLogging()) {
            if (tableForLogging.equals(query.fullTableName())) {
                logger.debug("Taable {} is eligible for logging.", query.fullTableName());
                return true;
            }
        }

        return false;
    }

}
