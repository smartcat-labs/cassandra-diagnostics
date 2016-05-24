package io.smartcat.cassandra.diagnostics;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.statements.ModificationStatement;
import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.Table;

/**
 * Class which will decide if statement should be logged or not.
 */
public class LogStatementDecider {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(LogStatementDecider.class);

    /**
     * Module configuration.
     */
    @Inject
    private Configuration config;

    /**
     * Check if statement should be logged or not based on rules. Table of statement should be for supported table and
     * execution time should be above threshold.
     *
     * @param execTime Execution time for statement
     * @param statement QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     *
     * @return decision whether we should log this statement or not.
     */
    public boolean logStatement(final long execTime, CQLStatement statement) {
        if (executionTimeForLogging(execTime) && tableForLogging(statement)) {
            return true;
        }

        return false;
    }

    private boolean executionTimeForLogging(final long execTime) {
        logger.trace("Checking if execution time:{} is above threshold: {}", execTime,
                config.slowQueryThresholdInMilliseconds);
        if (config.logAllQueries || execTime >= config.slowQueryThresholdInMilliseconds) {
            return true;
        }

        return false;
    }

    private boolean tableForLogging(CQLStatement statement) {
        logger.trace("Checking if table is in tables for logging.");
        if (config.tables.isEmpty()) {
            return true;
        }

        String table = "";
        String keyspace = "";

        if (statement instanceof SelectStatement) {
            table = ((SelectStatement) statement).columnFamily();
            keyspace = ((SelectStatement) statement).keyspace();
        }

        if (statement instanceof ModificationStatement) {
            table = ((ModificationStatement) statement).columnFamily();
            keyspace = ((SelectStatement) statement).keyspace();
        }

        if (StringUtils.isBlank(keyspace) || StringUtils.isBlank(table)) {
            logger.debug("Cannot extract table name.");
            return false;
        }

        for (Table supportedTable : config.tables) {
            if (supportedTable.keyspace.equals(keyspace) && supportedTable.name.equals(table)) {
                logger.debug("Taable {}.{} is eligible for logging.", keyspace, table);
                return true;
            }
        }

        return false;
    }

}
