package io.smartcat.cassandra.diagnostics.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.smartcat.cassandra.diagnostics.connector.ConnectorConfiguration;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.heartbeat.HeartbeatModule;
import io.smartcat.cassandra.diagnostics.reporter.LogReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

/**
 * This class represents the Cassandra Diagnostics configuration.
 */
public class Configuration {

    /**
     * Get default configuration for fallback when no configuration is provided.
     *
     * @return Configuration object with default {@code LogReporter} reporter
     */
    public static Configuration getDefaultConfiguration() {
        return new Configuration() {
            {
                final ReporterConfiguration reporter = new ReporterConfiguration();
                reporter.reporter = LogReporter.class.getName();
                reporters.add(reporter);

                Map<String, Object> options = new HashMap<>();
                options.put("period", 15);
                options.put("timeunit", TimeUnit.MINUTES.name());
                final ModuleConfiguration module = new ModuleConfiguration();
                module.measurement = "heartbeat";
                module.module = HeartbeatModule.class.getName();
                module.options = options;
                modules.add(module);

                connector = ConnectorConfiguration.getDefault();
            }
        };
    }

    /**
     * Connector-related configuration.
     */
    public ConnectorConfiguration connector = ConnectorConfiguration.getDefault();

    /**
     * System wide hostname. Set to override {@code InetAddress} querying.
     */
    public String hostname = null;

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
     * Reporters configuration list with reporter specific properties.
     */
    public List<ReporterConfiguration> reporters = new ArrayList<>();

    /**
     * Modules configuration list with module specific properties.
     */
    public List<ModuleConfiguration> modules = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ hostname: " + hostname);
        sb.append(", httpApiEnabled: " + httpApiEnabled);
        sb.append(", httpApiPort: " + httpApiPort);
        sb.append(", reporters: ");
        for (ReporterConfiguration reporter: reporters) {
            sb.append(reporter.toString());
        }
        sb.append(", modules: ");
        for (ModuleConfiguration module: modules) {
            sb.append(module.toString());
        }
        sb.append(" }");
        return sb.toString();
    }
}
