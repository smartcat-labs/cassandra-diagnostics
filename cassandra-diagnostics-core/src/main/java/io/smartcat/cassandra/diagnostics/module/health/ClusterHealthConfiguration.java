package io.smartcat.cassandra.diagnostics.module.health;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Cluster health module's configuration.
 */
public class ClusterHealthConfiguration {

    /**
     * A helper class for constructing immutable outer class.
     */
    public static class Values {
        private static final int DEFAULT_PERIOD = 10;
        private static final String DEFAULT_TIMEUNIT = "SECONDS";
        private static final boolean DEFAULT_NUMBER_OF_UNREACHABLE_NODES_ENABLED = false;

        /**
         * Cluster health reporting period.
         */
        public int period = DEFAULT_PERIOD;

        /**
         * Cluster health reporting time unit.
         */
        public TimeUnit timeunit = TimeUnit.valueOf(DEFAULT_TIMEUNIT);

        /**
         * Number of unreachable nodes.
         */
        public boolean numberOfUnreachableNodesEnabled = DEFAULT_NUMBER_OF_UNREACHABLE_NODES_ENABLED;
    }

    private Values values = new Values();

    private ClusterHealthConfiguration() {

    }

    /**
     * Create typed configuration for cluster health module out of generic module configuration.
     *
     * @param options Module configuration options.
     * @return typed cluster health module configuration from a generic one
     * @throws ConfigurationException in case the provided options are not valid
     */
    public static ClusterHealthConfiguration create(Map<String, Object> options) throws ConfigurationException {
        ClusterHealthConfiguration conf = new ClusterHealthConfiguration();
        Yaml yaml = new Yaml();
        String str = yaml.dumpAsMap(options);
        conf.values = yaml.loadAs(str, ClusterHealthConfiguration.Values.class);
        return conf;
    }

    /**
     * Cluster health reporting period.
     *
     * @return cluster health reporting period
     */
    public int period() {
        return values.period;
    }

    /**
     * Cluster health reporting time unit.
     *
     * @return cluster health reporting time unit
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
     * Number of unreachable nodes in the cluster.
     *
     * @return report number of unreachable nodes
     */
    public boolean numberOfUnreachableNodesEnabled() {
        return values.numberOfUnreachableNodesEnabled;
    }

}
