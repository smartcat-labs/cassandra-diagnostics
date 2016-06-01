package io.smartcat.cassandra.diagnostics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.cql3.statements.UpdateStatement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.config.Table;

public class LogStatementDeciderTest {

    private static final long EXECUTION_TIME_TO_LOG = 51;
    private static final long EXECUTION_TIME_NOT_TO_LOG = 49;

    private Injector INJECTOR = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            Configuration config = new Configuration();
            Table tableToLog = new Table();
            tableToLog.keyspace = "some_keyspace";
            tableToLog.name = "some_table";
            config.tables = Arrays.asList(tableToLog);
            bind(Configuration.class).toInstance(config);
        }
    });

    private LogStatementDecider logStatementDecider;

    private SelectStatement selectStatement;
    private UpdateStatement updateStatement;

    @Before
    public void setup() {
        logStatementDecider = INJECTOR.getInstance(LogStatementDecider.class);
        selectStatement = Mockito.mock(SelectStatement.class);
        updateStatement = Mockito.mock(UpdateStatement.class);
    }

    @Test
    public void do_not_log_query_when_execution_below_threshold() {
        boolean logStatement = logStatementDecider.logStatement(EXECUTION_TIME_NOT_TO_LOG, selectStatement);

        assertThat(logStatement).isFalse();
    }

    @Test
    public void do_not_log_query_when_statement_is_not_for_logged_table() {
        when(selectStatement.columnFamily()).thenReturn("not_supported_table");
        when(selectStatement.keyspace()).thenReturn("not_supported_keyspace");

        boolean logStatement = logStatementDecider.logStatement(EXECUTION_TIME_TO_LOG, selectStatement);

        assertThat(logStatement).isFalse();
    }

    @Test
    public void log_select_statement_when_statement_is_for_logged_table() {
        when(selectStatement.columnFamily()).thenReturn("some_table");
        when(selectStatement.keyspace()).thenReturn("some_keyspace");

        boolean logStatement = logStatementDecider.logStatement(EXECUTION_TIME_TO_LOG, selectStatement);

        assertThat(logStatement).isTrue();
    }

    @Test
    public void log_update_statement_when_statement_is_for_logged_table() {
        when(updateStatement.columnFamily()).thenReturn("some_table");
        when(updateStatement.keyspace()).thenReturn("some_keyspace");

        boolean logStatement = logStatementDecider.logStatement(EXECUTION_TIME_TO_LOG, updateStatement);

        assertThat(logStatement).isTrue();
    }

}
