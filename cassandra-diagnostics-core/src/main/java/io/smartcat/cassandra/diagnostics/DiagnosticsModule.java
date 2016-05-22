package io.smartcat.cassandra.diagnostics;

import com.google.inject.AbstractModule;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.config.ConfigurationLoader;
import io.smartcat.cassandra.diagnostics.config.YamlConfigurationLoader;
import io.smartcat.cassandra.diagnostics.jmx.DiagnosticsMXBean;
import io.smartcat.cassandra.diagnostics.jmx.DiagnosticsMXBeanImpl;
import io.smartcat.cassandra.diagnostics.report.ReporterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the main DI bindings.
 */
public class DiagnosticsModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticsModule.class);

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        ConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration config;

        try {
            config = loader.loadConfig();
        } catch (ConfigurationException e) {
            logger.warn("A problem occured while loading configuration. Using default configuration.", e);
            config = new Configuration();
        }
        logger.info("Effective configuration: {}", config);

        bind(ConfigurationLoader.class).toInstance(loader);
        bind(Configuration.class).toInstance(config);
        ReporterContext reporter = new ReporterContext(config);
        bind(ReporterContext.class).toInstance(reporter);
        bind(DiagnosticsMXBean.class).to(DiagnosticsMXBeanImpl.class);
    }

}
