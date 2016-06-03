package io.smartcat.cassandra.diagnostics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

/**
 * {@code DiagnosticsProcessor} creates instances of modules and reporters and provides reporter references to
 * modules per configuration.
 */
public class DiagnosticsProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticsProcessor.class);

    private List<Module> modules = new ArrayList<>();

    private Map<String, Reporter> reporters = new HashMap<>();

    /**
     * DiagnosticsProcessor constructor.
     *
     * @param configuration Configuration object
     */
    public DiagnosticsProcessor(Configuration configuration) {
        for (ReporterConfiguration reporterConfig : configuration.reporters) {
            try {
                logger.info("Creating reporter for class name {}", reporterConfig.reporter);
                Reporter reporter = (Reporter) Class.forName(reporterConfig.reporter)
                        .getConstructor(ReporterConfiguration.class).newInstance(reporterConfig);
                reporters.put(reporterConfig.reporter, reporter);
            } catch (Exception e) {
                logger.warn("Failed to create reporter by class name", e);
            }
        }

        for (ModuleConfiguration moduleConfig : configuration.modules) {
            try {
                logger.info("Creating module for class name {}", moduleConfig.module);
                Module module = (Module) Class.forName(moduleConfig.module).getConstructor(ModuleConfiguration.class)
                        .newInstance(moduleConfig);

                if (moduleConfig.reporters.isEmpty()) {
                    module.reporters.addAll(reporters.values());
                } else {
                    for (String reporterName : moduleConfig.reporters) {
                        module.reporters.add(reporters.get(reporterName));
                    }
                }
                modules.add(module);
            } catch (Exception e) {
                logger.warn("Failed to create module by class name", e);
            }
        }
    }

    /**
     * Process a query with all configured modules.
     *
     * @param query query to process
     */
    public void process(Query query) {
        for (Module module : modules) {
            module.process(query);
        }
    }

}
