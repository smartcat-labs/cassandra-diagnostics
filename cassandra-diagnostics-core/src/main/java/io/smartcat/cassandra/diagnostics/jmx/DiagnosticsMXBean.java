package io.smartcat.cassandra.diagnostics.jmx;

/**
 * JMX MXBean for monitoring and managing Cassandra Diagnostics module.
 */
public interface DiagnosticsMXBean {
    /**
     * Reload diagnostics configuration.
     */
    public void reload();
}
