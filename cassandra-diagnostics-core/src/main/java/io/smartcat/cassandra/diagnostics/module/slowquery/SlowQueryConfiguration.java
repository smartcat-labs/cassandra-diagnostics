package io.smartcat.cassandra.diagnostics.module.slowquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Typed configuration class with reasonable defaults for this module.
 */
public class SlowQueryConfiguration {

    /**
     * A helper class for constructing immutable outer class.
     */
    public static class Values {
        private static final int DEFAULT_SLOW_QUERY_THRESHOLD = 25;
        private static final boolean DEFAULT_SLOW_QUERY_REPORT_ENABLED = false;
        private static final boolean DEFAULT_SLOW_QUERY_COUNT_REPORT_ENABLED = true;
        private static final int DEFAULT_SLOW_QUERY_COUNT_REPORT_PERIOD = 1;
        private static final String DEFAULT_SLOW_QUERY_COUNT_REPORT_TIMEUNIT = "MINUTES";
        private static final List<String> DEFAULT_SLOW_QUERY_TYPES_TO_LOG = Arrays.asList("ALL");

        /**
         * Query execution's threshold.
         */
        public int slowQueryThresholdInMilliseconds = DEFAULT_SLOW_QUERY_THRESHOLD;
        /**
         * Table names to filter queries.
         */
        public List<String> tablesForLogging = new ArrayList<String>();

        /**
         * Slow query reporting enabled.
         */
        public boolean slowQueryReportEnabled = DEFAULT_SLOW_QUERY_REPORT_ENABLED;

        /**
         * Slow query count reporting enabled.
         */
        public boolean slowQueryCountReportEnabled = DEFAULT_SLOW_QUERY_COUNT_REPORT_ENABLED;

        /**
         * Slow query count reporting period.
         */
        public int slowQueryCountReportPeriod = DEFAULT_SLOW_QUERY_COUNT_REPORT_PERIOD;

        /**
         * Slow query count reporting time unit.
         */
        public TimeUnit slowQueryCountReportTimeunit = TimeUnit.valueOf(DEFAULT_SLOW_QUERY_COUNT_REPORT_TIMEUNIT);

        /**
         * Slow query statement types to be logged. Only the types in the list will be logged.
         */
        public List<String> queryTypesToLog = DEFAULT_SLOW_QUERY_TYPES_TO_LOG;
    }

    private Values values = new Values();

    private SlowQueryConfiguration() {
    }

    /**
     * Creates typed configuration for slow query module out of generic module configuration.
     *
     * @param options Module configuration options.
     * @return typed slow query module configuration from a generic one.
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public static SlowQueryConfiguration create(Map<String, Object> options) throws ConfigurationException {
        SlowQueryConfiguration conf = new SlowQueryConfiguration();
        Yaml yaml = new Yaml();
        String str = yaml.dumpAsMap(options);
        try {
            conf.values = yaml.loadAs(str, Values.class);
        } catch (Exception e) {
            throw new ConfigurationException("Unable to load configuration.", e);
        }
        return conf;
    }

    /**
     * Threshold for reporting slow queries.
     *
     * @return threshold for slow queries.
     */
    public int slowQueryThreshold() {
        return values.slowQueryThresholdInMilliseconds;
    }

    /**
     * List of tables to log slow queries on.
     *
     * @return list of full table names (keyspace.table) to use when logging slow queries.
     */
    public List<String> tablesForLogging() {
        return Collections.unmodifiableList(values.tablesForLogging);
    }

    /**
     * Should report each slow query.
     *
     * @return should report each slow query
     */
    public boolean slowQueryReportEnabled() {
        return values.slowQueryReportEnabled;
    }

    /**
     * Should report slow query count.
     *
     * @return should report slow query count
     */
    public boolean slowQueryCountReportEnabled() {
        return values.slowQueryCountReportEnabled;
    }

    /**
     * Slow query count reporting period.
     *
     * @return slow query count reporting period
     */
    public int slowQueryCountReportPeriod() {
        return values.slowQueryCountReportPeriod;
    }

    /**
     * Slow query count reporting time unit.
     *
     * @return slow query count reporting time unit
     */
    public TimeUnit slowQueryCountReportTimeunit() {
        return values.slowQueryCountReportTimeunit;
    }

    /**
     * Slow query count reporting rate in milliseconds.
     *
     * @return Slow query count reporting rate in milliseconds
     */
    public long slowQueryCountReportingRateInMillis() {
        return slowQueryCountReportTimeunit().toMillis(slowQueryCountReportPeriod());
    }

    /**
     * Slow query statement types to be logged. Only the types in the list will be logged.
     *
     * @return List of query types to be logged.
     */
    public List<String> queryTypesToLog() {
        return values.queryTypesToLog;
    }

}
