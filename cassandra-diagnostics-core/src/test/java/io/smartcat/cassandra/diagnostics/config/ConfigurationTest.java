package io.smartcat.cassandra.diagnostics.config;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.module.heartbeat.HeartbeatModule;
import io.smartcat.cassandra.diagnostics.reporter.LogReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;


public class ConfigurationTest {

    @Test
    public void deafult_connector_configuration() {
        Configuration conf = Configuration.getDefaultConfiguration();
        assertThat(conf.connector.queuedEventsOverflowThreshold).isEqualTo(1000);
        assertThat(conf.connector.numWorkerThreads).isEqualTo(2);
    }

    @Test
    public void default_configuration() {
        Configuration conf = Configuration.getDefaultConfiguration();
        assertThat(conf.reporters.size()).isEqualTo(1);
        assertThat(conf.modules.size()).isEqualTo(1);

        ReporterConfiguration reporterConfiguration = conf.reporters.get(0);
        assertThat(reporterConfiguration.reporter).isEqualTo(LogReporter.class.getName());

        ModuleConfiguration moduleConfiguration = conf.modules.get(0);
        assertThat(moduleConfiguration.module).isEqualTo(HeartbeatModule.class.getName());
        assertThat(moduleConfiguration.measurement).isEqualTo("heartbeat");
        assertThat(moduleConfiguration.options.get("period")).isEqualTo(15);
        assertThat(moduleConfiguration.options.get("timeunit")).isEqualTo(TimeUnit.MINUTES.name());
    }

    @Test
    public void test_hostname() throws ConfigurationException {
        System.setProperty("cassandra.diagnostics.config", "valid-cassandra-diagnostics.yml");
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();
        assertThat(configuration.hostname).isEqualTo("test-hostname");
    }

}
