package io.smartcat.cassandra.diagnostics.module.heartbeat;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Heartbeat module's configuration.
 */
public class HeartbeatConfiguration {

    /**
     * A helper class for constructing immutable outer class.
     */
    public static class Values {
        private static final int DEFAULT_PERIOD = 15;
        private static final String DEFAULT_TIMEUNIT = "MINUTES";

        /**
         * Heartbeat period.
         */
        public int period = DEFAULT_PERIOD;
        /**
         * Heartbeat period's time unit.
         */
        public TimeUnit timeunit = TimeUnit.valueOf(DEFAULT_TIMEUNIT);
    }

    private Values values = new Values();

    private HeartbeatConfiguration() {
    }

    /**
     * Create typed configuration for heartbeat module out of generic module configuration.
     *
     * @param options Module configuration options.
     * @return typed heartbeat module configuration from a generic one
     * @throws ConfigurationException in case the provided options are not valid
     */
    public static HeartbeatConfiguration create(Map<String, Object> options) throws ConfigurationException {
        HeartbeatConfiguration conf = new HeartbeatConfiguration();
        Yaml yaml = new Yaml();
        String str = yaml.dumpAsMap(options);
        conf.values = yaml.loadAs(str, HeartbeatConfiguration.Values.class);
        return conf;
    }

    /**
     * Heartbeat period getter.
     * @return heartbeat period
     */
    public int period() {
        return values.period;
    }

    /**
     * Heartbeat period time unit getter.
     * @return heartbeat time unit
     */
    public TimeUnit timeunit() {
        return values.timeunit;
    }

    /**
     * Period in milliseconds.
     * @return heartbeat period in milliseconds
     */
    public long periodInMillis() {
        return timeunit().toMillis(period());
    }
}
