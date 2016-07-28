package io.smartcat.cassandra.diagnostics.connector;

/**
 * Connector implementation related configuration.
 */
public class Configuration {

    /**
     * The number of worker threads that asynchronously process
     * diagnostics events.
     */
    public int numWorkerThreads = 2;

    /**
     * The maximum number of diagnostics events waiting to be processed.
     * Once this number is reached, new events are being rejected.
     */
    public int maxQueuedEvents = 1000;
}
