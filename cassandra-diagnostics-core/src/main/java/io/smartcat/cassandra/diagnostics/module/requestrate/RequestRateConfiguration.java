package io.smartcat.cassandra.diagnostics.module.requestrate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Reqiest rate module's ocnfiguration.
 */
public class RequestRateConfiguration {

    /**
     * A helper class for constructing immutable outer class.
     */
    public static class Values {
        private static final int DEFAULT_PERIOD = 1;
        private static final String DEFAULT_TIMEUNIT = "SECONDS";
        private static final boolean DEFAULT_SEPARATE_BY_REQUEST_TYPE = true;

        /**
         * Request rate reporting period.
         */
        public int period = DEFAULT_PERIOD;

        /**
         * Request rate reporting time unit.
         */
        public TimeUnit timeunit = TimeUnit.valueOf(DEFAULT_TIMEUNIT);

        /**
         * Indicates if rates are reported for request types or combined.
         */
        public boolean separateByRequestType = DEFAULT_SEPARATE_BY_REQUEST_TYPE;
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
     * Indicates if rates are reported for request types or combined.
     *
     * @return Combined or separate request type report
     */
    public boolean separateByRequestType() {
        return values.separateByRequestType;
    }

}
