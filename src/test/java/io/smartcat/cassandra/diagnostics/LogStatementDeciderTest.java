package io.smartcat.cassandra.diagnostics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.apache.cassandra.cql3.statements.SelectStatement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.config.ConfigurationLoader;
import io.smartcat.cassandra.diagnostics.config.YamlConfigurationLoader;

public class LogStatementDeciderTest {

    private Injector INJECTOR = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            ConfigurationLoader loader = new YamlConfigurationLoader();
            Configuration config;

            try {
                config = loader.loadConfig();
            } catch (ConfigurationException e) {
                config = new Configuration();
            }

            bind(Configuration.class).toInstance(config);
        }
    });

    private LogStatementDecider logStatementDecider;

    private SelectStatement statement;

    @Before
    public void setup() {
        logStatementDecider = INJECTOR.getInstance(LogStatementDecider.class);
        statement = Mockito.mock(SelectStatement.class);
    }

    @Test
    public void do_not_log_query_when_execution_below_threshold() {
        long execTime = 20;

        boolean logStatement = logStatementDecider.logStatement(execTime, statement);

        assertThat(logStatement).isFalse();
    }

    @Test
    public void do_not_log_query_when_statement_is_not_for_logged_table() {
        long execTime = 26;
        when(statement.columnFamily()).thenReturn("not_supported_table");
        when(statement.keyspace()).thenReturn("not_supported_keyspace");

        boolean logStatement = logStatementDecider.logStatement(execTime, statement);

        assertThat(logStatement).isFalse();
    }

    @Test
    public void log_query_when_statement_is_for_logged_table() {
        long execTime = 26;
        when(statement.columnFamily()).thenReturn("some_table");
        when(statement.keyspace()).thenReturn("some_keyspace");

        boolean logStatement = logStatementDecider.logStatement(execTime, statement);

        assertThat(logStatement).isTrue();
    }

}
