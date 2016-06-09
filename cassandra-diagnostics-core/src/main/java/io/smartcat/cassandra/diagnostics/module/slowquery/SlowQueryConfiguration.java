package io.smartcat.cassandra.diagnostics.module.slowquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;

/**
 * Typed configuration class with reasonable defaults for this module.
 *
 */
public class SlowQueryConfiguration {

    private static final String DEFAULT_SLOW_QUERY_THRESHOLD = "25";
    private static final String DEFAULT_LOG_ALL_QUERIES = "true";

    private final int slowQueryThreshold;

    private final boolean logAllQueries;

    private final List<String> tablesForLogging;

    private SlowQueryConfiguration(int slowQueryThreshold, boolean logAllQueries, List<String> tablesForLogging) {
        super();
        this.slowQueryThreshold = slowQueryThreshold;
        this.logAllQueries = logAllQueries;
        this.tablesForLogging = tablesForLogging;
    }

    /**
     * Create typed configuration for slow query module out of generic module configuration.
     * @param configuration Module configuration.
     * @return typed slow query module configuration from generic one.
     */
    public static SlowQueryConfiguration create(ModuleConfiguration configuration) {
        int slowQueryThreshold = Integer.parseInt(
                configuration.getDefaultOption("slowQueryThresholdInMilliseconds", DEFAULT_SLOW_QUERY_THRESHOLD));
        boolean logAllQueries = Boolean
                .parseBoolean(configuration.getDefaultOption("logAllQueries", DEFAULT_LOG_ALL_QUERIES));
        String tables = configuration.options.get("tablesForLogging");
        if (StringUtils.isBlank(tables)) {
            return new SlowQueryConfiguration(slowQueryThreshold, logAllQueries, new ArrayList<String>());
        } else {
            return new SlowQueryConfiguration(slowQueryThreshold, logAllQueries,
                    Arrays.asList(StringUtils.split(tables, '|')));
        }
    }

    /**
     * Threshold for reporting slow queries.
     * @return threshold for slow queries.
     */
    public int slowQueryThreshold() {
        return slowQueryThreshold;
    }

    /**
     * Switch to log all queries or just slow queries.
     * @return boolean value for this switch
     */
    public boolean logAllQueries() {
        return logAllQueries;
    }

    /**
     * List of tables to log slow queries on.
     * @return list of full table names (keyspace.table) to use when logging slow queries.
     */
    public List<String> tablesForLogging() {
        return tablesForLogging;
    }

}
