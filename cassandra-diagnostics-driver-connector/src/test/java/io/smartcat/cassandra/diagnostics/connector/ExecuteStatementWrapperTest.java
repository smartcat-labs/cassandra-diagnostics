package io.smartcat.cassandra.diagnostics.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

import io.smartcat.cassandra.diagnostics.Query;

public class ExecuteStatementWrapperTest {

    public class TestQueryReporter implements QueryReporter {
        public final CountDownLatch lock = new CountDownLatch(1);
        public Query reportedQuery;
        @Override
        public void report(Query query) {
            reportedQuery = query;
            lock.countDown();
        }
    }

    @Test
    public void process_regular_select_statement() throws Exception {
        TestQueryReporter reporter = new TestQueryReporter();
        Configuration configuration = new Configuration();
        ExecuteStatementWrapper wrapper = new ExecuteStatementWrapper(reporter, configuration);

        RegularStatement statement = mock(RegularStatement.class);
        Session session = mock(Session.class);
        ResultSetFuture result = mock(ResultSetFuture.class);

        when(session.executeAsync(any(Statement.class))).thenReturn(result);
        when(statement.getKeyspace()).thenReturn("test_keyspace");
        when(statement.getQueryString()).thenReturn("SELECT * FROM test_table WHERE id = 1");

        wrapper.processStatement(statement, System.currentTimeMillis(), result);

        reporter.lock.await(1000, TimeUnit.MILLISECONDS);

        assertThat(reporter.reportedQuery.keyspace()).isEqualTo("test_keyspace");
        assertThat(reporter.reportedQuery.statement()).isEqualTo("SELECT * FROM test_table WHERE id = 1;");
        assertThat(reporter.reportedQuery.statementType()).isEqualTo(Query.StatementType.SELECT);
    }

    @Test
    public void process_regular_update_statement() throws Exception {
        TestQueryReporter reporter = new TestQueryReporter();
        Configuration configuration = new Configuration();
        ExecuteStatementWrapper wrapper = new ExecuteStatementWrapper(reporter, configuration);

        RegularStatement statement = mock(RegularStatement.class);
        Session session = mock(Session.class);
        ResultSetFuture result = mock(ResultSetFuture.class);

        when(session.executeAsync(any(Statement.class))).thenReturn(result);
        when(statement.getKeyspace()).thenReturn("test_keyspace");
        when(statement.getQueryString()).thenReturn("INSERT INTO test_table");

        wrapper.processStatement(statement, System.currentTimeMillis(), result);

        reporter.lock.await(1000, TimeUnit.MILLISECONDS);

        assertThat(reporter.reportedQuery.keyspace()).isEqualTo("test_keyspace");
        assertThat(reporter.reportedQuery.statement()).isEqualTo("INSERT INTO test_table;");
        assertThat(reporter.reportedQuery.statementType()).isEqualTo(Query.StatementType.UPDATE);
    }

    @Test
    public void process_bound_select_statement() throws Exception {
        TestQueryReporter reporter = new TestQueryReporter();
        Configuration configuration = new Configuration();
        ExecuteStatementWrapper wrapper = new ExecuteStatementWrapper(reporter, configuration);

        BoundStatement statement = mock(BoundStatement.class);
        Session session = mock(Session.class);
        ResultSetFuture result = mock(ResultSetFuture.class);
        PreparedStatement pstm = mock(PreparedStatement.class);

        when(session.executeAsync(any(Statement.class))).thenReturn(result);
        when(statement.getKeyspace()).thenReturn("test_keyspace");
        when(statement.preparedStatement()).thenReturn(pstm);
        when(pstm.getQueryString()).thenReturn("SELECT * FROM test_table WHERE id = 1");

        wrapper.processStatement(statement, System.currentTimeMillis(), result);

        reporter.lock.await(1000, TimeUnit.MILLISECONDS);

        assertThat(reporter.reportedQuery.keyspace()).isEqualTo("test_keyspace");
        assertThat(reporter.reportedQuery.statement()).isEqualTo("SELECT * FROM test_table WHERE id = 1;");
        assertThat(reporter.reportedQuery.statementType()).isEqualTo(Query.StatementType.SELECT);
        System.out.println(reporter.reportedQuery);
    }

}
