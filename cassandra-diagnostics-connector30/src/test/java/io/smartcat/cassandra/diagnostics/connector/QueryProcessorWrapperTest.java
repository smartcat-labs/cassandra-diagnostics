package io.smartcat.cassandra.diagnostics.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.constructorsDeclaredIn;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import io.smartcat.cassandra.diagnostics.query.Query;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.apache.cassandra.service.ClientState")
@PrepareForTest({ ClientState.class })
public class QueryProcessorWrapperTest {

    private Query reportedQuery;
    private CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void wrapper_intercepts_query_with_all_data() throws NoSuchFieldException, SecurityException, Exception {
        QueryReporter queryReporter = new QueryReporter() {
            @Override
            public void report(Query query) {
                reportedQuery = query;
                lock.countDown();
            }
        };
        ConnectorConfiguration configuration = new ConnectorConfiguration();
        QueryProcessorWrapper wrapper = new QueryProcessorWrapper(queryReporter, configuration);

        SelectStatement statement = mock(SelectStatement.class);
        when(statement.keyspace()).thenReturn("test_keyspace");
        when(statement.columnFamily()).thenReturn("test_table");

        QueryState queryState = mock(QueryState.class);
        suppress(constructorsDeclaredIn(ClientState.class));
        ClientState clientState = ClientState.forInternalCalls();
        setFinal(clientState, clientState.getClass().getDeclaredField("isInternal"), false);
        setFinal(clientState, clientState.getClass().getDeclaredField("remoteAddress"),
                new InetSocketAddress("172.31.0.1", 1000));
        when(queryState.getClientState()).thenReturn(clientState);

        QueryOptions options = mock(QueryOptions.class);
        when(options.getConsistency()).thenReturn(ConsistencyLevel.ONE);

        wrapper.processPrepared(statement, queryState, options, System.currentTimeMillis(), null, null);

        lock.await(1000, TimeUnit.MILLISECONDS);

        assertThat(reportedQuery.clientAddress()).isEqualTo(new InetSocketAddress("172.31.0.1", 1000).toString());
        assertThat(reportedQuery.statementType()).isEqualTo(Query.StatementType.SELECT);
        assertThat(reportedQuery.keyspace()).isEqualTo("test_keyspace");
        assertThat(reportedQuery.tableName()).isEqualTo("test_table");
        assertThat(reportedQuery.consistencyLevel()).isEqualTo(Query.ConsistencyLevel.ONE);
    }

    @Test
    public void connector_reporter_does_not_report_internal_query_with_all_data() throws Exception {
        QueryReporter queryReporter = new QueryReporter() {
            @Override
            public void report(Query query) {
                reportedQuery = query;
                lock.countDown();
            }
        };

        ConnectorConfiguration configuration = new ConnectorConfiguration();
        QueryProcessorWrapper wrapper = new QueryProcessorWrapper(queryReporter, configuration);

        SelectStatement statement = mock(SelectStatement.class);
        when(statement.keyspace()).thenReturn("test_keyspace");
        when(statement.columnFamily()).thenReturn("test_table");

        QueryState queryState = mock(QueryState.class);
        suppress(constructorsDeclaredIn(ClientState.class));
        ClientState clientState = ClientState.forInternalCalls();
        setFinal(clientState, clientState.getClass().getDeclaredField("isInternal"), true);
        when(queryState.getClientState()).thenReturn(clientState);

        QueryOptions options = mock(QueryOptions.class);

        wrapper.processPrepared(statement, queryState, options, System.currentTimeMillis(), null, null);
        lock.await(1000, TimeUnit.MILLISECONDS);

        assertThat(reportedQuery).isEqualTo(null);
    }

    private void setFinal(Object target, Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(target, newValue);
    }

}
