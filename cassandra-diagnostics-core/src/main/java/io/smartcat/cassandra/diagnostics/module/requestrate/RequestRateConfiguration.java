package io.smartcat.cassandra.diagnostics.module.requestrate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Request rate module's configuration.
 */
public class RequestRateConfiguration {

    /**
     * Default is to report all requests.
     */
    public static final String ALL_REQUESTS_TO_REPORT = "ALL";

    /**
     * Since request to report is combination between statement type and consistency level delimiter is used to glue
     * those values together.
     */
    public static final String REQUEST_META_DELIMITER = ":";

    /**
     * A helper class for constructing immutable outer class.
     */
    public static class Values {
        private static final int DEFAULT_PERIOD = 1;
        private static final String DEFAULT_TIMEUNIT = "SECONDS";
        private static final List<String> DEFAULT_REQUESTS_TO_REPORT = Arrays.asList(ALL_REQUESTS_TO_REPORT);

        /**
         * Request rate reporting period.
         */
        public int period = DEFAULT_PERIOD;

        /**
         * Request rate reporting time unit.
         */
        public TimeUnit timeunit = TimeUnit.valueOf(DEFAULT_TIMEUNIT);

        /**
         * Combination of statement type and consistency level to report.
         */
        public List<String> requestsToReport = DEFAULT_REQUESTS_TO_REPORT;
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

        try {
            conf.values = yaml.loadAs(str, RequestRateConfiguration.Values.class);
        } catch (Exception e) {
            throw new ConfigurationException("Unable to load configuration.", e);
        }

        validateRequestsToReport(conf);

        return conf;
    }

    private static void validateRequestsToReport(RequestRateConfiguration conf) throws ConfigurationException {
        for (String requestToReport : conf.requestsToReport()) {
            if (ALL_REQUESTS_TO_REPORT.equals(requestToReport)) {
                continue;
            }

            String[] requestMeta = requestToReport.split(REQUEST_META_DELIMITER);

            if (requestMeta.length != 2) {
                throw new ConfigurationException(
                        "Only two configuration parameters supported, statement type and consistency level.");
            }

            String statementType = requestMeta[0];
            String consistencyLevel = requestMeta[1];

            try {
                Query.StatementType.valueOf(statementType);
            } catch (IllegalArgumentException ex) {
                throw new ConfigurationException("Illegal statement type configured: " + statementType);
            }

            try {
                Query.ConsistencyLevel.valueOf(consistencyLevel);
             } catch (IllegalArgumentException ex) {
                throw new ConfigurationException("Illegal consistency level configured: " + consistencyLevel);
             }
        }
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

    /**
     * Type of requests to report (combination of consistency level and statement type).
     *
     * @return list of requests to report
     */
    public List<String> requestsToReport() {
        return values.requestsToReport;
    }
}
