package io.smartcat.cassandra.diagnostics;

import java.util.ServiceLoader;

import io.smartcat.cassandra.diagnostics.connector.ConnectorProxy;

/**
 * Connector proxy factory class for obtaining {@link ConnectorProxy} SPI implementation.
 */
public class ConnectorProxyFactory {

    private ConnectorProxyFactory() {
    }

    /**
     * Returns SPI {@link ConnectorProxy} implementation.
     *
     * @return {@link ConnectorProxy} implementation
     */
    public static ConnectorProxy getImplementation() {
        ServiceLoader<ConnectorProxy> loader = ServiceLoader.load(ConnectorProxy.class);
        if (loader.iterator().hasNext()) {
            return loader.iterator().next();
        } else {
            throw new IllegalStateException("Unable to find Cassandra Connector implementation.");
        }
    }
}
