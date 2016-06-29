package io.smartcat.cassandra.diagnostics.module.requestrate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Request rate module's configuration.
 */
public class RequestRateConfiguration {

    /**
     * A helper class for constructing immutable outer class.
     */
    public static class Values {
        private static final int DEFAULT_PERIOD = 1;
        private static final String DEFAULT_TIMEUNIT = "SECONDS";

        /**
         * Request rate reporting period.
         */
        public int period = DEFAULT_PERIOD;

        /**
         * Request rate reporting time unit.
         */
        public TimeUnit timeunit = TimeUnit.valueOf(DEFAULT_TIMEUNIT);
    }

    private Values values = new Values();

    private RequestRateConfiguration() {

    }

    /**
     * Create typed configuration for request rate module out of generic module configuration.
     *
     * @param options Module configuration options.
     * @return types request rate module configuration from a generic one
     * @throws ConfigurationException in case the provided options are not valid
     */
    public static RequestRateConfiguration create(Map<String, Object> options) throws ConfigurationException {
        RequestRateConfiguration conf = new RequestRateConfiguration();
        Yaml yaml = new Yaml();
        String str = yaml.dumpAsMap(options);
        conf.values = yaml.loadAs(str, RequestRateConfiguration.Values.class);
        return conf;
    }

    /**
     * Request rate reporting period.
     *
     * @return request rate reporting period
     */
    public int period() {
        return values.period;
    }

    /**
     * Request rate reporting time unit.
     *
     * @return request rate reporting time unit
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
