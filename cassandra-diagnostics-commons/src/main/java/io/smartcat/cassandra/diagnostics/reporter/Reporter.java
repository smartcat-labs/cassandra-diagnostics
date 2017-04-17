package io.smartcat.cassandra.diagnostics.reporter;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * Reporter abstraction that forces a constructor signature. All valid reporters should extend this class.
 */
public abstract class Reporter {

    /**
     * Reporter configuration.
     */
    protected ReporterConfiguration configuration;

    /**
     * Diagnostics global configuration.
     */
    protected GlobalConfiguration globalConfiguration;

    /**
     * Constructor.
     *
     * @param configuration        Reporter configuration
     * @param globalConfiguration  Diagnostics configuration
     */
    public Reporter(ReporterConfiguration configuration, GlobalConfiguration globalConfiguration) {
        this.configuration = configuration;
        this.globalConfiguration = globalConfiguration;
    }

    /**
     * Reports an intercepted query.
     *
     * @param measurement processed information about intercepted query
     */
    public abstract void report(Measurement measurement);

    /**
     * Used to gracefully stop reporter.
     */
    public void stop() {

    }
}
