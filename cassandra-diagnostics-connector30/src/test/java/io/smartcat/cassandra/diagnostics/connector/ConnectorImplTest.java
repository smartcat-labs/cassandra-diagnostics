package io.smartcat.cassandra.diagnostics.connector;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.instrument.InstrumentationSavingAgent;

public class ConnectorImplTest {

    private static Instrumentation instrumentation;

    @BeforeClass
    public static void setUp() {
        instrumentation = InstrumentationSavingAgent.getInstrumentation();
    }

    @Test
    public void invokes_wrapper_when_query_processor_activates() throws Exception {
        ConnectorConfiguration configuration = new ConnectorConfiguration();
        Connector connector = new ConnectorImpl();
        connector.init(instrumentation, mock(QueryReporter.class), configuration);
        QueryProcessorWrapper queryProcessorWrapper = mock(QueryProcessorWrapper.class);
        setStatic(ConnectorImpl.class.getDeclaredField("queryProcessorWrapper"), queryProcessorWrapper);

        QueryProcessor queryProcessor = QueryProcessor.instance;

        CQLStatement statement = mock(CQLStatement.class);
        QueryState queryState = mock(QueryState.class);
        QueryOptions options = mock(QueryOptions.class);
        queryProcessor.processStatement(statement, queryState, options);

        verify(queryProcessorWrapper).processStatement(
                same(statement), same(queryState), same(options),
                any(Long.class), any(ResultMessage.class), any(Throwable.class));
    }

    private void setStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        field.set(null, newValue);
    }

}
