package io.smartcat.cassandra.diagnostics.jmx;

import io.smartcat.cassandra.diagnostics.Diagnostics;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Diagnostics JMX MXBean.
 */
public class DiagnosticsMXBeanImpl implements DiagnosticsMXBean {

    /**
     * Module configuration.
     */
    private Configuration config;

    private Diagnostics diagnostics;

    /**
     * Constructor.
     *
     * @param config configuration object
     * @param diagnostics diagnostics instance
     */
    public DiagnosticsMXBeanImpl(Configuration config, Diagnostics diagnostics) {
        this.config = config;
        this.diagnostics = diagnostics;
    }

    /**
     * Diagnostics' configuration reload operation.
     */
    public void reload() {
        diagnostics.reload();
    }
}
