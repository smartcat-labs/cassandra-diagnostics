package io.smartcat.cassandra.diagnostics.module.hiccup;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

import io.smartcat.cassandra.diagnostics.config.ConfigurationException;

/**
 * Hiccup module's configuration.
 */
public class HiccupConfiguration {

    /**
     * A helper class for constructing immutable outer class.
     */
    public static class Values {

        private static final double DEFAULT_RESOLUTION_IN_MS = 1.0;
        private static final long DEFAULT_START_DELAY_IN_MS = 30000;
        private static final boolean DEFAULT_ALLOCATE_OBJECTS = false;

        // default to ~20usec best-case resolution
        private static final long DEFAULT_LOWEST_TRACKABLE_VALUE_IN_NANOS = 1000L * 20L;
        private static final long DEFAULT_HIGHEST_TRACKABLE_VALUE_IN_NANOS = 3600 * 1000L * 1000L * 1000L;
        private static final int DEFAULT_NUMBER_OF_SIGNIFICANT_VALUE_DIGITS = 2;

        private static final int DEFAULT_PERIOD = 5;
        private static final String DEFAULT_TIMEUNIT = "SECONDS";

        /**
         * Sleep resolution in millis.
         */
        public double resolutionInMs = DEFAULT_RESOLUTION_IN_MS;

        /**
         * Initial start delay in millis.
         */
        public long startDelayInMs = DEFAULT_START_DELAY_IN_MS;

        /**
         * Allocate objects to measure object creation stalls.
         */
        public boolean allocateObjects = DEFAULT_ALLOCATE_OBJECTS;

        /**
         * Lowest trackable value.
         */
        public long lowestTrackableValueInNanos = DEFAULT_LOWEST_TRACKABLE_VALUE_IN_NANOS;

        /**
         * Highest trackable value.
         */
        public long highestTrackableValueInNanos = DEFAULT_HIGHEST_TRACKABLE_VALUE_IN_NANOS;

        /**
         * Number of significat value digits.
         */
        public int numberOfSignificantValueDigits = DEFAULT_NUMBER_OF_SIGNIFICANT_VALUE_DIGITS;

        /**
         * Hiccup reporting period.
         */
        public int period = DEFAULT_PERIOD;

        /**
         * Hiccup reporting period's time unit.
         */
        public TimeUnit timeunit = TimeUnit.valueOf(DEFAULT_TIMEUNIT);
    }

    private Values values = new Values();

    private HiccupConfiguration() {
    }

    /**
     * Create typed configuration for hiccup module out of generic module configuration.
     *
     * @param options Module configuration options.
     * @return typed hiccup module configuration from a generic one
     * @throws ConfigurationException in case the provided options are not valid
     */
    public static HiccupConfiguration create(Map<String, Object> options) throws ConfigurationException {
        HiccupConfiguration conf = new HiccupConfiguration();
        Yaml yaml = new Yaml();
        String str = yaml.dumpAsMap(options);
        conf.values = yaml.loadAs(str, HiccupConfiguration.Values.class);
        return conf;
    }

    /**
     * Hiccup sleep resolution in millis.
     *
     * @return hiccup sleep resolution
     */
    public double resolutionInMs() {
        return values.resolutionInMs;
    }

    /**
     * Hiccup initial start delay in millis.
     *
     * @return hiccup initial start delay
     */
    public long startDelayInMs() {
        return values.startDelayInMs;
    }

    /**
     * Allocate objects to measure object creation stalls.
     *
     * @return allocate objects
     */
    public boolean allocateObjects() {
        return values.allocateObjects;
    }

    /**
     * Lowest trackable value.
     *
     * @return lowest trackable value
     */
    public long lowestTrackableValueInNanos() {
        return values.lowestTrackableValueInNanos;
    }

    /**
     * Highest trackable value.
     *
     * @return highest trackable value
     */
    public long highestTrackableValueInNanos() {
        return values.highestTrackableValueInNanos;
    }

    /**
     * Number of significat value digits.
     *
     * @return number of significat value digits
     */
    public int numberOfSignificantValueDigits() {
        return values.numberOfSignificantValueDigits;
    }

    /**
     * Hiccup reporting period getter.
     *
     * @return hiccup reporting period
     */
    public int period() {
        return values.period;
    }

    /**
     * Hiccup reporting period time unit getter.
     *
     * @return hiccup reporting time unit
     */
    public TimeUnit timeunit() {
        return values.timeunit;
    }

    /**
     * Hiccup reporting interval in milliseconds.
     *
     * @return reporting interval in milliseconds
     */
    public long reportingIntervalInMillis() {
        return timeunit().toMillis(period());
    }

}
