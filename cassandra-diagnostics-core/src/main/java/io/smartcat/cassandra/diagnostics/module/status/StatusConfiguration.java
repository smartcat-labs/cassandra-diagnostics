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

        /**
         * Status reporting period.
         */
        public int period = DEFAULT_PERIOD;

        /**
         * Status reporting time unit.
         */
        public TimeUnit timeunit = TimeUnit.valueOf(DEFAULT_TIMEUNIT);
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

}
