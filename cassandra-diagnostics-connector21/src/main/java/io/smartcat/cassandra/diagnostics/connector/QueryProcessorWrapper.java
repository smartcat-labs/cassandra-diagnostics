package io.smartcat.cassandra.diagnostics.connector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * This class is a Diagnostics wrapper for {@link org.apache.cassandra.cql3.QueryProcessor}.
 */
public class QueryProcessorWrapper {

    private static final Logger logger = LoggerFactory.getLogger(QueryProcessorWrapper.class);

    /**
     * The number of threads used for executing query reports.
     */
    private static final int EXECUTOR_NO_THREADS = 2;

    /**
    private QueryReporter queryReporter;

    public QueryProcessorWrapper(QueryReporter queryReporter) {
        this.queryReporter = queryReporter;
    }

    /**
     * Wrapper for
     * {@link org.apache.cassandra.cql3.QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)} method.
     * This method implements the same functionality as the original method and, in addition, measures the statement
     * execution time and reports the query if it is larger than the configured execution time threshold.
     *
     * @param statement  QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param queryState QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param options    QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param origLogger internal class logger of {@link org.apache.cassandra.cql3.QueryProcessor}
     * @return QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @throws RequestExecutionException  QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @throws RequestValidationException QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     */
    public ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options,
            Logger origLogger) throws RequestExecutionException, RequestValidationException {
        originalProcessStatement(statement, queryState, options, origLogger);
        /*
        ResultMessage result;
        final long startTime = System.currentTimeMillis();
        try {

            result = originalProcessStatement(statement, queryState, options, origLogger);

            final long execTime = System.currentTimeMillis() - startTime;

            report(startTime, execTime, statement, queryState, options, null);

            return result == null ? new ResultMessage.Void() : result;

        } catch (Exception err) {
            final long execTime = System.currentTimeMillis() - startTime;
            report(startTime, execTime, statement, queryState, options, err.getMessage());
            throw err;
        }
        */
        return null;

    }

    private ResultMessage originalProcessStatement(CQLStatement statement, QueryState queryState, QueryOptions options,
            Logger logger) throws RequestExecutionException, RequestValidationException {

        logger.trace("Process {} @CL.{}", statement, options.getConsistency());
        ClientState clientState = queryState.getClientState();
        statement.checkAccess(clientState);
        statement.validate(clientState);

        ResultMessage result = statement.execute(queryState, options);

        return result == null ? new ResultMessage.Void() : result;
    }

}
