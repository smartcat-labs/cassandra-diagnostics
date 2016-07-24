package io.smartcat.cassandra.diagnostics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

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

    private static final String PROCESSING_THREAD_NAME = "cassandra-diagnostics-processor";

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticsProcessor.class);

    private List<Module> modules = new ArrayList<>();

    private Map<String, Reporter> reporters = new HashMap<>();

    private final LinkedBlockingQueue<Query> queryBufferQueue;

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

        queryBufferQueue = new LinkedBlockingQueue<>(configuration.queryQueueCapacity);
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                process();
            }
        });
        th.setName(PROCESSING_THREAD_NAME);
        th.start();

        initReporters(configuration.reporters);
        initModules(configuration.modules);
    }

    private void initReporters(final List<ReporterConfiguration> reportersConfiguration) {
        for (ReporterConfiguration reporterConfig : reportersConfiguration) {
            try {
                logger.info("Creating reporter for class name {}", reporterConfig.reporter);
                Reporter reporter = (Reporter) Class.forName(reporterConfig.reporter)
                        .getConstructor(ReporterConfiguration.class).newInstance(reporterConfig);
                reporters.put(reporterConfig.reporter, reporter);
            } catch (Exception e) {
                logger.warn("Failed to create reporter by class name", e);
            }
        }
    }

    private void initModules(final List<ModuleConfiguration> modulesConfiguration) {
        for (ModuleConfiguration moduleConfig : modulesConfiguration) {
            try {
                logger.info("Creating module for class name {}", moduleConfig.module);
                final Module module = createModule(moduleConfig);
                modules.add(module);
            } catch (Exception e) {
                logger.warn("Failed to create module by class name", e);
            }
        }
    }

    private Module createModule(final ModuleConfiguration moduleConfiguration) throws Exception {
        final List<Reporter> refs = new ArrayList<>();

        if (moduleConfiguration.reporters == null || moduleConfiguration.reporters.isEmpty()) {
            logger.info("Assigning all available reporters to module {}", moduleConfiguration.module);
            refs.addAll(reporters.values());
        } else {
            List<Reporter> moduleReporters = getModuleReporters(moduleConfiguration.reporters);
            if (moduleReporters.isEmpty()) {
                throw new IllegalStateException("Module does not have any reporter assigned.");
            }
            refs.addAll(moduleReporters);
        }

        final Module module = (Module) Class.forName(moduleConfiguration.module)
                .getConstructor(ModuleConfiguration.class, List.class).newInstance(moduleConfiguration, refs);

        return module;
    }

    private List<Reporter> getModuleReporters(final List<String> reporterNames) {
        final ArrayList<Reporter> moduleReporters = new ArrayList<>();
        for (String reporterName : reporterNames) {
            if (reporters.containsKey(reporterName)) {
                moduleReporters.add(reporters.get(reporterName));
            } else {
                logger.warn("Unknown reporter specified as module reporter: {}", reporterName);
            }
        }
        return moduleReporters;
    }

    /**
     * Accepts a query sent by the connector. with all configured modules.
     *
     * @param query query to process
     */
    public void process(final Query query) {
        logger.trace("Accepting query {}", query);
        if (!queryBufferQueue.offer(query)) {
            // the queue is full
            logger.warn("The query buffer queueu is full. The reported query is ignored.");
        }
    }

    private void process() {
        while (true) {
            try {
                Query query = queryBufferQueue.take();
                logger.trace("Processing query {}", query);
                for (Module module : modules) {
                    module.process(query);
                }
            } catch (InterruptedException e) {
                logger.info(PROCESSING_THREAD_NAME + " thread interrupted.");
                break;
            }
        }
    }

}
