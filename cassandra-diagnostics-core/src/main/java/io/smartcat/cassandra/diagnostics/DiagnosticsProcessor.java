package io.smartcat.cassandra.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.query.Query;

/**
 * {@code DiagnosticsProcessor} creates instances of modules and reporters and provides reporter references to
 * modules per configuration.
 */
public class DiagnosticsProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticsProcessor.class);

//    private List<Module> modules = new ArrayList<>();
//
//    private Map<String, Reporter> reporters = new HashMap<>();

    /**
     * DiagnosticsProcessor constructor.
     *
     * @param configuration Configuration object
     */
    public DiagnosticsProcessor(final Configuration configuration) {
        if (configuration.reporters == null) {
            throw new IllegalStateException("Configuration does not have any reporter defined.");
        }

        if (configuration.modules == null) {
            throw new IllegalStateException("Configuration does not have any module defined.");
        }

        if (configuration.global == null) {
            throw new IllegalStateException("Configuration does not have global configuration defined.");
        }

//        initReporters(configuration.reporters, configuration.global);
//        initModules(configuration.modules, configuration.global);
    }

//    private void initReporters(final List<ReporterConfiguration> reportersConfiguration,
//            final GlobalConfiguration globalConfiguration) {
//        for (ReporterConfiguration reporterConfig : reportersConfiguration) {
//            try {
//                logger.info("Creating reporter for class name {}", reporterConfig.reporter);
//                Reporter reporter = (Reporter) Class.forName(reporterConfig.reporter)
//                        .getConstructor(ReporterConfiguration.class, GlobalConfiguration.class)
//                        .newInstance(reporterConfig, globalConfiguration);
//                reporters.put(reporterConfig.reporter, reporter);
//            } catch (Exception e) {
//                logger.warn("Failed to create reporter by class name", e);
//            }
//        }
//    }
//
//    private void initModules(final List<ModuleConfiguration> modulesConfiguration,
//            final GlobalConfiguration globalConfiguration) {
//        for (ModuleConfiguration moduleConfig : modulesConfiguration) {
//            try {
//                logger.info("Creating module for class name {}", moduleConfig.module);
//                final Module module = createModule(moduleConfig, globalConfiguration);
//                modules.add(module);
//            } catch (Exception e) {
//                logger.warn("Failed to create module by class name", e);
//            }
//        }
//    }
//
//    private Module createModule(final ModuleConfiguration moduleConfiguration,
//            final GlobalConfiguration globalConfiguration) throws Exception {
//        final List<Reporter> moduleReporters = new ArrayList<>();
//
//        if (moduleConfiguration.reporters == null || moduleConfiguration.reporters.isEmpty()) {
//            logger.info("Assigning all available reporters to module {}", moduleConfiguration.module);
//            moduleReporters.addAll(reporters.values());
//        } else {
//            List<Reporter> reporters = getModuleReporters(moduleConfiguration.reporters);
//            if (reporters.isEmpty()) {
//                throw new IllegalStateException("Module does not have any reporter assigned.");
//            }
//            moduleReporters.addAll(reporters);
//        }
//
//        final Module module = (Module) Class.forName(moduleConfiguration.module)
//                .getConstructor(ModuleConfiguration.class, List.class, GlobalConfiguration.class)
//                .newInstance(moduleConfiguration, moduleReporters, globalConfiguration);
//
//        return module;
//    }
//
//    private List<Reporter> getModuleReporters(final List<String> reporterNames) {
//        final ArrayList<Reporter> moduleReporters = new ArrayList<>();
//        for (String reporterName : reporterNames) {
//            if (reporters.containsKey(reporterName)) {
//                moduleReporters.add(reporters.get(reporterName));
//            } else {
//                logger.warn("Unknown reporter specified as module reporter: {}", reporterName);
//            }
//        }
//        return moduleReporters;
//    }

    /**
     * Process a query with all configured modules.
     *
     * @param query query to process
     */
    public void process(final Query query) {
//        logger.trace("Processing query {}", query);
//        for (Module module : modules) {
//            module.process(query);
//        }
    }

    /**
     * Gracefully stop all modules and reporters.
     */
    public void shutdown() {
//        logger.info("Shutting down modules.");
//        for (Module module : modules) {
//            module.stop();
//        }
//        logger.info("Shutting down reporters.");
//        for (Reporter reporter : reporters.values()) {
//            reporter.stop();
//        }
    }
}
