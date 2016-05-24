package io.smartcat.cassandra.diagnostics;

import io.smartcat.cassandra.diagnostics.connector.Connector;

import java.util.ServiceLoader;

/**
 * Factory class for obtaining {@link Connector} SPI implementation.
 */
public class ConnectorFactory {

    private ConnectorFactory() {
    }

    /**
     * Returns SPI {@code Connector} implementation.
     *
     * @return {@code Connector} implementation
     */
    public static Connector getImplementation() {
        ServiceLoader<Connector> loader = ServiceLoader.load(Connector.class);
        if (loader.iterator().hasNext()) {
            return loader.iterator().next();
        } else {
            throw new IllegalStateException("Unable to find Cassandra Connector implementation.");
        }
    }
}
