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
    public void default_connector_configuration() {
        Configuration conf = Configuration.getDefaultConfiguration();
        assertThat(conf.connector.numWorkerThreads).isEqualTo(2);
        assertThat(conf.connector.queuedEventsOverflowThreshold).isEqualTo(1000);
        assertThat(conf.connector.queuedEventsRelaxThreshold).isEqualTo(700);
        assertThat(conf.connector.jmxHost).isEqualTo("127.0.0.1");
        assertThat(conf.connector.jmxPort).isEqualTo(7199);
        assertThat(conf.connector.jmxAuthEnabled).isEqualTo(false);
        assertThat(conf.connector.jmxUsername).isEqualTo(null);
        assertThat(conf.connector.jmxPassword).isEqualTo(null);
    }

    @Test
    public void default_configuration() {
        Configuration conf = Configuration.getDefaultConfiguration();
        assertThat(conf.reporters.size()).isEqualTo(1);
        assertThat(conf.modules.size()).isEqualTo(1);
        assertThat(conf.global.httpApiEnabled).isTrue();
        assertThat(conf.global.httpApiHost).isEqualTo("127.0.0.1");
        assertThat(conf.global.httpApiPort).isEqualTo(8998);
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
        assertThat(configuration.global.hostname).isEqualTo("test-hostname");
    }

    @Test
    public void test_http_api_settings() throws ConfigurationException {
        System.setProperty("cassandra.diagnostics.config", "valid-cassandra-diagnostics.yml");
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();
        assertThat(configuration.global.httpApiEnabled).isEqualTo(true);
        assertThat(configuration.global.httpApiHost).isEqualTo("10.0.0.1");
        assertThat(configuration.global.httpApiPort).isEqualTo(8001);
    }

    @Test
    public void test_systemname() throws ConfigurationException {
        System.setProperty("cassandra.diagnostics.config", "valid-cassandra-diagnostics.yml");
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();
        assertThat(configuration.global.systemName).isEqualTo("smartcat-cassandra-cluster");
    }

    @Test
    public void test_connector_configuration() throws ConfigurationException {
        System.setProperty("cassandra.diagnostics.config", "valid-cassandra-diagnostics.yml");
        YamlConfigurationLoader loader = new YamlConfigurationLoader();
        Configuration configuration = loader.loadConfig();
        assertThat(configuration.connector.jmxHost).isEqualTo("10.0.0.1");
        assertThat(configuration.connector.jmxPort).isEqualTo(8888);
        assertThat(configuration.connector.jmxAuthEnabled).isEqualTo(true);
        assertThat(configuration.connector.jmxUsername).isEqualTo("username");
        assertThat(configuration.connector.jmxPassword).isEqualTo("password");
    }

}
