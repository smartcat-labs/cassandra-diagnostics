package io.smartcat.cassandra.diagnostics.connector;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.service.QueryState;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.instrument.InstrumentationSavingAgent;

public class ConnectorImplTest {

    private static Instrumentation instrumentation;

    @BeforeClass
    public static void setUp() {
        instrumentation = InstrumentationSavingAgent.getInstrumentation();
    }

    @Test
    public void instrumentation_initializes_transformer() {
        QueryReporter queryReporter = mock(QueryReporter.class);
        Instrumentation inst = mock(Instrumentation.class);
        ConnectorImpl connector = new ConnectorImpl();
        connector.init(inst, queryReporter);

        verify(inst).addTransformer(any(ClassFileTransformer.class), anyBoolean());
    }

    @Test
    public void invokes_wrapper_when_query_processor_activates() throws Exception {
        Connector connector = new ConnectorImpl();
        connector.init(instrumentation, mock(QueryReporter.class));
        QueryProcessorWrapper queryProcessorWrapper = mock(QueryProcessorWrapper.class);
        setStatic(ConnectorImpl.class.getDeclaredField("queryProcessorWrapper"), queryProcessorWrapper);

        QueryProcessor queryProcessor = QueryProcessor.instance;

        CQLStatement statement = mock(CQLStatement.class);
        QueryState queryState = mock(QueryState.class);
        QueryOptions options = mock(QueryOptions.class);
        queryProcessor.processStatement(statement, queryState, options);

        verify(queryProcessorWrapper).processStatement(
                same(statement), same(queryState), same(options), any(Logger.class));
    }

    private void setStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        field.set(null, newValue);
    }

}
