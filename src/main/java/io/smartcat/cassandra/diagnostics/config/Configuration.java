package io.smartcat.cassandra.diagnostics.config;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Cassandra Diagnostics configuration.
 */
public class Configuration {

    /**
     * Get default configuration for fallback when no configuration is provided.
     *
     * @return Configuration object with default LogQueryReporter reporter
     */
    public static Configuration getDefaultConfiguration() {
        return new Configuration() {
            {
                reporters.add(new ReporterConfiguration());
            }
        };
    }

    /**
     * Query execution time threshold. A query whose execution time is grater than this value is reported. The execution
     * time is expressed in milliseconds.
     */
    public long slowQueryThresholdInMilliseconds = 50;

    /**
     * Configuration option to log all queries instead of just slow queries.
     */
    public boolean logAllQueries = false;

    /**
     * Reporters configuration list with reporter specific properties.
     */
    public List<ReporterConfiguration> reporters = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ slowQueryThresholdInMilliseconds: ").append(slowQueryThresholdInMilliseconds)
                .append(", logAllQueries: ").append(logAllQueries).append(", reporters: ");
        reporters.forEach(reporter -> sb.append(reporter.toString()));
        return sb.toString();
    }

}
