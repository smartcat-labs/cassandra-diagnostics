package io.smartcat.cassandra.diagnostics.reporter;

import io.smartcat.cassandra.diagnostics.config.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

/**
 * Reporter abstract class.
 */
public abstract class Reporter {

    /**
     * Reporter configuration.
     */
    protected ReporterConfiguration reporterConfiguration;

    /**
     * Global configuration.
     */
    protected GlobalConfiguration globalConfiguration;

    /**
     * Constructor.
     *
     * @param reporterConfiguration reporter configuration
     * @param globalConfiguration   global configuration
     */
    public Reporter(final ReporterConfiguration reporterConfiguration, final GlobalConfiguration globalConfiguration) {
        this.reporterConfiguration = reporterConfiguration;
        this.globalConfiguration = globalConfiguration;
    }

    /**
     * Reporter start method.
     */
    public void start() {

    }

    /**
     * Reporter stop method.
     */
    public void stop() {

    }

    /**
     * Report measurement.
     *
     * @param measurement measurement object
     */
    public abstract void report(final Measurement measurement);

}
