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
     * The number of diagnostics events waiting to be processed that
     * once reached, new events are being dropped until the number of queued events
     * dropped to <code>queuedEventsRelaxThreshold</code>.
     */
    public int queuedEventsOverflowThreshold = 1000;

    /**
     * The number of diagnostics events waiting to be processed that
     * once reached, after the queue previously was in the overflow state, new events are being queued again,
     * until the number of queued events dropped to <code>queuedEventsOverflowThreshold</code>.
     */
    public int queuedEventsRelaxThreshold = 700;

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
