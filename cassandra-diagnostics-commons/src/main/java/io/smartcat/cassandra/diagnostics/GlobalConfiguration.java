package io.smartcat.cassandra.diagnostics;

import io.smartcat.cassandra.diagnostics.utils.Utils;

/**
 * Global configuration for Cassandra diagnostics.
 */
public class GlobalConfiguration {

    /**
     * System wide hostname. Set to override {@code InetAddress} querying.
     */
    public String hostname = Utils.resolveHostname();

    /**
     * System wide name. Set to differentiate between systems under observation.
     */
    public String systemName = "cassandra-cluster";

    /**
     * Enables diagnostics HTTP API.
     */
    public Boolean httpApiEnabled = true;

    /**
     * Host name for binding HTTP API listening socket.
     */
    public String httpApiHost = "127.0.0.1";

    /**
     * TCP port for diagnostics HTTP API.
     */
    public Integer httpApiPort = 8998;

    /**
     * Enables HTTP API key-based authentication.
     */
    public Boolean httpApiAuthEnabled = false;

    /**
     * HTTP API access key.
     */
    public String httpApiKey = "diagnostics-api-key";

    /**
     * Returns the default configuration.
     *
     * @return default configuration
     */
    public static GlobalConfiguration getDefault() {
        return new GlobalConfiguration();
    }

}
