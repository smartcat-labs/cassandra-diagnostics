package io.smartcat.cassandra.diagnostics.api;

/**
 * JMX MXBean for monitoring and managing Cassandra Diagnostics module.
 */
public interface DiagnosticsApi {
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
