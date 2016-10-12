package io.smartcat.cassandra.diagnostics.jmx;

/**
 * JMX MXBean for monitoring and managing Cassandra Diagnostics module.
 */
public interface DiagnosticsMXBean {
    /**
     * Cassandra Diagnostics version.
     * @return version string
     */
    public String getVersion();

    /**
     * Reload diagnostics configuration.
     */
    public void reload();
}
