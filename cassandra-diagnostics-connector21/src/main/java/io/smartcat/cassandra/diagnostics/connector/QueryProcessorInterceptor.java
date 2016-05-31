package io.smartcat.cassandra.diagnostics.connector;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.QueryState;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;

/**
 * Defines instrumentation intercepter for {@link org.apache.cassandra.cql3.QueryProcessor}.
 */
public class QueryProcessorInterceptor {

    /**
     * Prevents class instantiation.
     */
    private QueryProcessorInterceptor() {
    }

    /**
     * Intercepter for
     * {@link org.apache.cassandra.cql3.QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}. Every
     * invocation is being delegated to {@link QueryProcessorWrapper}.
     *
     * @param statement QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param queryState QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param options QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param logger internal class logger of {@link org.apache.cassandra.cql3.QueryProcessor}
     * @return QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @throws RequestExecutionException QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @throws RequestValidationException QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     */
    @RuntimeType
    public static void processStatement() {

        //return ConnectorImpl.queryProcessorWrapper.processStatement();
        return;
    }

}
