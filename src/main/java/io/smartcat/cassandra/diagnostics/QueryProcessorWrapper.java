package io.smartcat.cassandra.diagnostics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;

import io.smartcat.cassandra.diagnostics.report.QueryReport;
import io.smartcat.cassandra.diagnostics.report.ReporterContext;

/**
 * This class is a Diagnostics wrapper for {@link org.apache.cassandra.cql3.QueryProcessor}. It reports CQL queries that
 * are executed slower than the configured execution threshold.
 */
public class QueryProcessorWrapper {

    /**
     * The number of threads used for executing query reports.
     */
    private static final int EXECUTOR_NO_THREADS = 2;

    /**
     * Executor service used for executing query reports.
     */
    private static ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_NO_THREADS,
            new ThreadFactoryBuilder().setNameFormat("Cassandra-Diagnostics-%d").setPriority(Thread.MIN_PRIORITY)
                    .build());

    /**
     * {@link QueryReport} used for query reporting.
     */
    @Inject
    private ReporterContext reporter;

    /**
     * LogStatementDecider to decide if statement should be logged.
     */
    @Inject
    private LogStatementDecider logStatementDecider;

    /**
     * Wrapper for
     * {@link org.apache.cassandra.cql3.QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)} method.
     * This method implements the same functionality as the original method and, in addition, measures the statement
     * execution time and reports the query if it is larger than the configured execution time threshold.
     *
     * @param statement QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param queryState QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param options QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param origLogger internal class logger of {@link org.apache.cassandra.cql3.QueryProcessor}
     * @return QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @throws RequestExecutionException QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @throws RequestValidationException QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     */
    public ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options,
            Logger origLogger) throws RequestExecutionException, RequestValidationException {

        ResultMessage result;
        final long startTime = System.currentTimeMillis();
        try {

            result = originalProcessStatement(statement, queryState, options, origLogger);
            final long execTime = System.currentTimeMillis() - startTime;

            if (logStatementDecider.logStatement(execTime, statement)) {
                report(startTime, execTime, statement, queryState, options, null);
            }

            return result == null ? new ResultMessage.Void() : result;

        } catch (Exception err) {
            final long execTime = System.currentTimeMillis() - startTime;
            report(startTime, execTime, statement, queryState, options, err.getMessage());
            throw err;
        }

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

    /**
     * Submits a query reports asynchronously.
     *
     * @param startTime execution start time, in milliseconds
     * @param execTime execution time, in milliseconds
     * @param statement CQL statement
     * @param queryState CQL query state
     * @param options CQL query options
     * @param errorMessage error message in case there was a problem during query execution
     */
    private void report(final long startTime, final long execTime, final CQLStatement statement,
            final QueryState queryState, final QueryOptions options, final String errorMessage) {
        if (queryState.getClientState().isInternal) {
            return;
        }

        executor.submit(() -> {
            reporter.report(new QueryReport(startTime, execTime,
                    queryState.getClientState().getRemoteAddress().toString(), statement.getClass().getSimpleName()));
        });
    }

}
