package io.smartcat.cassandra.diagnostics.module.slowquery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Typed configuration class with reasonable defaults for this module.
 *
 */
public class SlowQueryConfiguration {

    /**
     * A helper class for constructing immutable outer class.
     */
    public static class Values {
        private static final int DEFAULT_SLOW_QUERY_THRESHOLD = 25;

        /**
         * Query execution's threshold.
         */
        public int slowQueryThresholdInMilliseconds = DEFAULT_SLOW_QUERY_THRESHOLD;
        /**
         * Table names to filter queries.
         */
        public List<String> tablesForLogging = new ArrayList<String>();
    }

    private Values values = new Values();

    private SlowQueryConfiguration() {
    }

    /**
     * Creates typed configuration for slow query module out of generic module configuration.
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
     * @return threshold for slow queries.
     */
    public int slowQueryThreshold() {
        return values.slowQueryThresholdInMilliseconds;
    }

    /**
     * List of tables to log slow queries on.
     * @return list of full table names (keyspace.table) to use when logging slow queries.
     */
    public List<String> tablesForLogging() {
        return Collections.unmodifiableList(values.tablesForLogging);
    }
}
