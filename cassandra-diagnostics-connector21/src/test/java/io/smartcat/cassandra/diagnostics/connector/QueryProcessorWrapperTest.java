package io.smartcat.cassandra.diagnostics.connector;

import static org.mockito.Mockito.*;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.QueryState;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.slf4j.Logger;

public class QueryProcessorWrapperTest {

    @Test
    public void test() throws RequestExecutionException, RequestValidationException {
        QueryReporter queryReporter = mock(QueryReporter.class);
        QueryProcessorWrapper wrapper = new QueryProcessorWrapper(queryReporter);

        CQLStatement statement = mock(CQLStatement.class);
        QueryState queryState = mock(QueryState.class);
        QueryOptions options = mock(QueryOptions.class);
        Logger origLogger = mock(Logger.class);

        wrapper.processStatement(statement, queryState, options, origLogger);

    }

}
