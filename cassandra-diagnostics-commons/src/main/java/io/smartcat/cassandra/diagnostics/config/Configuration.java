package io.smartcat.cassandra.diagnostics.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.smartcat.cassandra.diagnostics.connector.ConnectorConfiguration;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
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
                reporter.reporter = "io.smartcat.cassandra.diagnostics.reporter.LogReporter";
                reporters.add(reporter);

                Map<String, Object> options = new HashMap<>();
                options.put("period", 15);
                options.put("timeunit", TimeUnit.MINUTES.name());
                final ModuleConfiguration module = new ModuleConfiguration();
                module.measurement = "heartbeat";
                module.module = "io.smartcat.cassandra.diagnostics.module.heartbeat.HeartbeatModule";
                module.options = options;
                modules.add(module);
            }
        };
    }

    /**
     * Connector-related configuration.
     */
    public ConnectorConfiguration connector = ConnectorConfiguration.getDefault();

    /**
     * Global configuration.
     */
    public GlobalConfiguration global = GlobalConfiguration.getDefault();

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
        sb.append("{ hostname: " + global.hostname);
        sb.append("{ systemName: " + global.systemName);
        sb.append(", httpApiEnabled: " + global.httpApiEnabled);
        sb.append(", httpApiPort: " + global.httpApiPort);
        sb.append(", reporters: ");
        for (ReporterConfiguration reporter : reporters) {
            sb.append(reporter.toString());
        }
        sb.append(", modules: ");
        for (ModuleConfiguration module : modules) {
            sb.append(module.toString());
        }
        sb.append(" }");
        return sb.toString();
    }
}
