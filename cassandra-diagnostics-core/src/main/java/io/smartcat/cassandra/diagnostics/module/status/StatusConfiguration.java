package io.smartcat.cassandra.diagnostics.module.status;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Status module's configuration.
 */
public class StatusConfiguration {

    /**
     * A helper class for constructing immutable outer class.
     */
    public static class Values {
        private static final int DEFAULT_PERIOD = 1;
        private static final String DEFAULT_TIMEUNIT = "MINUTES";
        private static final boolean DEFAULT_COMPACTIONS_ENABLED = false;
        private static final boolean DEFAULT_TPSTATS_ENABLED = false;
        private static final boolean DEFAULT_REPAIRS_ENABLED = false;
        private static final boolean DEFAULT_NODE_INFO_ENABLED = false;

        /**
         * Status reporting period.
         */
        public int period = DEFAULT_PERIOD;

        /**
         * Status reporting time unit.
         */
        public TimeUnit timeunit = TimeUnit.valueOf(DEFAULT_TIMEUNIT);

        /**
         * Status of compactions.
         */
        public boolean compactionsEnabled = DEFAULT_COMPACTIONS_ENABLED;

        /**
         * Status of threadpools.
         */
        public boolean tpStatsEnabled = DEFAULT_TPSTATS_ENABLED;

        /**
         * Status of repairs.
         */
        public boolean repairsEnabled = DEFAULT_REPAIRS_ENABLED;

        /**
         * Node related information.
         */
        public boolean nodeInfoEnabled = DEFAULT_NODE_INFO_ENABLED;
    }

    private Values values = new Values();

    private StatusConfiguration() {

    }

    /**
     * Create typed configuration for status module out of generic module configuration.
     *
     * @param options Module configuration options.
     * @return typed status module configuration from a generic one
     * @throws ConfigurationException in case the provided options are not valid
     */
    public static StatusConfiguration create(Map<String, Object> options) throws ConfigurationException {
        StatusConfiguration conf = new StatusConfiguration();
        Yaml yaml = new Yaml();
        String str = yaml.dumpAsMap(options);
        conf.values = yaml.loadAs(str, StatusConfiguration.Values.class);
        return conf;
    }

    /**
     * Status reporting period.
     *
     * @return status reporting period
     */
    public int period() {
        return values.period;
    }

    /**
     * Status reporting time unit.
     *
     * @return status reporting time unit
     */
    public TimeUnit timeunit() {
        return values.timeunit;
    }

    /**
     * Reporting rate in milliseconds.
     *
     * @return reporting rate in milliseconds
     */
    public long reportingRateInMillis() {
        return timeunit().toMillis(period());
    }

    /**
     * Status of compactions is being reported.
     *
     * @return report status of compactions
     */
    public boolean compactionsEnabled() {
        return values.compactionsEnabled;
    }

    /**
     * Status of thread pools is being reported.
     *
     * @return report status of thread pools
     */
    public boolean tpStatsEnabled() {
        return values.tpStatsEnabled;
    }

    /**
     * Status of repairs is being reported.
     *
     * @return report status of repairs
     */
    public boolean repairsEnabled() {
        return values.repairsEnabled;
    }

    /**
     * Node information.
     *
     * @return true iff node info is enabled.
     */
    public boolean nodeInfoEnabled() {
        return values.nodeInfoEnabled;
    }

}
