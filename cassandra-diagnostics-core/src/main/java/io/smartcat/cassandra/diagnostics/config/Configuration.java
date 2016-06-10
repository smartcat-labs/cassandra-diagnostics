package io.smartcat.cassandra.diagnostics.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.heartbeat.HeartbeatModule;
import io.smartcat.cassandra.diagnostics.report.LogQueryReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

/**
 * This class represents the Cassandra Diagnostics configuration.
 */
public class Configuration {

    /**
     * Get default configuration for fallback when no configuration is provided.
     *
     * @return Configuration object with default {@code LogQueryReporter} reporter
     */
    public static Configuration getDefaultConfiguration() {
        return new Configuration() {
            {
                final ReporterConfiguration reporter = new ReporterConfiguration();
                reporter.reporter = LogQueryReporter.class.getName();
                reporters.add(reporter);

                Map<String, Object> options = new HashMap<>();
                options.put("period", 15);
                options.put("timeunit", TimeUnit.MINUTES.name());
                final ModuleConfiguration module = new ModuleConfiguration();
                module.measurement = "heartbeat";
                module.module = HeartbeatModule.class.getName();
                module.options = options;
                modules.add(module);
            }
        };
    }

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
        sb.append("{  reporters: ");
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
