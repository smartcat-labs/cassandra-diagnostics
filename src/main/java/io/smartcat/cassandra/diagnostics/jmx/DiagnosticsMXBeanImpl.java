package io.smartcat.cassandra.diagnostics.jmx;

import com.google.inject.Inject;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Diagnostics JMX MXBean.
 */
public class DiagnosticsMXBeanImpl implements DiagnosticsMXBean {

    /**
     * Module configuration.
     */
    private Configuration config;

    /**
     * Constructor.
     *
     * @param config configuration object
     */
    @Inject
    public DiagnosticsMXBeanImpl(Configuration config) {
        this.config = config;
    }

    @Override
    public long getSlowQueryThreshold() {
        return config.slowQueryThreshold;
    }

    @Override
    public void setSlowQueryThreshold(long value) {
        config.slowQueryThreshold = value;
    }

}
