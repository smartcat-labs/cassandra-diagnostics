package io.smartcat.cassandra.diagnostics;

import com.google.inject.AbstractModule;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.config.ConfigurationLoader;
import io.smartcat.cassandra.diagnostics.config.YamlConfigurationLoader;
import io.smartcat.cassandra.diagnostics.jmx.DiagnosticsMXBean;
import io.smartcat.cassandra.diagnostics.jmx.DiagnosticsMXBeanImpl;
import io.smartcat.cassandra.diagnostics.report.LogQueryReporter;
import io.smartcat.cassandra.diagnostics.report.QueryReporter;

/**
 * Defines the main DI bindings.
 */
public class DiagnosticsModule extends AbstractModule {

  @SuppressWarnings("unchecked")
  @Override
  protected void configure() {
    ConfigurationLoader loader = new YamlConfigurationLoader();
    Configuration config;

    try {
      config = loader.loadConfig();
    } catch (ConfigurationException e) {
      config = new Configuration();
    }

    bind(ConfigurationLoader.class).toInstance(loader);
    bind(Configuration.class).toInstance(config);

    try {
      bind(QueryReporter.class).to((Class<? extends QueryReporter>) Class.forName(config.reporter));
    } catch (ClassNotFoundException e) {
      bind(QueryReporter.class).to(LogQueryReporter.class);
    }

    bind(DiagnosticsMXBean.class).to(DiagnosticsMXBeanImpl.class);
  }

}
