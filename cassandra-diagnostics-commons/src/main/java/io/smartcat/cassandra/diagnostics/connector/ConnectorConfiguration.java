package io.smartcat.cassandra.diagnostics.connector;

/**
 * Connector implementation related configuration.
 */
public class ConnectorConfiguration {

    /**
     * The number of worker threads that asynchronously process
     * diagnostics events.
     */
    public int numWorkerThreads = 2;

    /**
     * Configured threshold for queue size, above this threshold all events will be
     * dropped until the number of queued events is dropped to
     * <code>queuedEventsRelaxThreshold</code>.
     */
    public int queuedEventsOverflowThreshold = 1000;

    /**
     * Lower threshold bound for event queue size. After the queue was previously
     * in overflow state, new events will be queued only when the number of queued
     * events drop below this value.
     */
    public int queuedEventsRelaxThreshold = 700;

    /**
     * Whether to enable tracing or not. It is useful for various modules when debugging.
     */
    public boolean enableTracing = false;

    /**
     * Returns the default configuration.
     * @return default configuration
     */
    public static ConnectorConfiguration getDefault() {
        return new ConnectorConfiguration();
    }

    /**
     * Node JMX host.
     */
    public String jmxHost = "127.0.0.1";

    /**
     * Node JMX port.
     */
    public Integer jmxPort = 7199;

    /**
     * Node JMX authentication enabled.
     */
    public Boolean jmxAuthEnabled = false;

    /**
     * Node JMX authentication username.
     */
    public String jmxUsername = null;

    /**
     * Node JMX authentication password.
     */
    public String jmxPassword = null;

}
