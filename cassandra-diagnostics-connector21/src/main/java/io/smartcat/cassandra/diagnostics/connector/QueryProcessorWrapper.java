package io.smartcat.cassandra.diagnostics.connector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.statements.ModificationStatement;
import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Query;

/**
 * This class is a Diagnostics wrapper for {@link org.apache.cassandra.cql3.QueryProcessor}.
 */
public class QueryProcessorWrapper {

    private static final Logger logger = LoggerFactory.getLogger(QueryProcessorWrapper.class);

    /**
     * The number of threads used for executing query reports.
     */
    private static final int EXECUTOR_NO_THREADS = 2;

    private static final AtomicLong THREAD_COUNT = new AtomicLong(0);

    /**
     * Executor service used for executing query reports.
     */
    private static ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_NO_THREADS,
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable);
                    thread.setName("cassandra-diagnostics-connector-" + THREAD_COUNT.getAndIncrement());
                    thread.setDaemon(true);
                    thread.setPriority(Thread.MIN_PRIORITY);
                    return thread;
                }
            });

    private QueryReporter queryReporter;

    /**
     * Constructor.
     *
     * @param queryReporter QueryReporter used to report queries
     */
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
     * @param startTime    execution start time, in milliseconds
     * @param execTime     execution time, in milliseconds
     * @param statement    CQL statement
     * @param queryState   CQL query state
     * @param options      CQL query options
     * @param errorMessage error message in case there was a problem during query execution
     */
    private void report(final long startTime, final long execTime, final CQLStatement statement,
            final QueryState queryState, final QueryOptions options, final String errorMessage) {
        if (queryState.getClientState().isInternal) {
            return;
        }

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Query query = extractQuery(startTime, execTime, statement, queryState, options, errorMessage);
                    logger.trace("Reporting query: {}.", query);
                    queryReporter.report(query);
                } catch (Exception e) {
                    logger.warn("An error occured while reporting query", e);
                }
            }
        });
    }

    private Query extractQuery(final long startTime, final long execTime, final CQLStatement statement,
            final QueryState queryState, final QueryOptions options, final String errorMessage) {
        Query query;
        if (statement instanceof SelectStatement) {
            query = extractQuery(startTime, execTime, (SelectStatement) statement, queryState,
                    options, errorMessage);
        } else if (statement instanceof ModificationStatement) {
            query = extractQuery(startTime, execTime, (ModificationStatement) statement, queryState,
                    options, errorMessage);
        } else {
            query = extractGenericQuery(startTime, execTime, statement, queryState, options, errorMessage);
        }
        return query;
    }

    private Query extractQuery(final long startTime, final long execTime, final SelectStatement statement,
            final QueryState queryState, final QueryOptions options, final String errorMessage) {
        //statement.getSelection().getColumnMapping().
        return Query.create(
                startTime,
                execTime,
                queryState.getClientState().getRemoteAddress().toString(),
                Query.StatementType.SELECT,
                statement.keyspace(),
                statement.columnFamily(),
                "",
                errorMessage);
    }

    private Query extractQuery(final long startTime, final long execTime, final ModificationStatement statement,
            final QueryState queryState, final QueryOptions options, final String errorMessage) {
        return Query.create(
                startTime,
                execTime,
                queryState.getClientState().getRemoteAddress().toString(),
                Query.StatementType.UPDATE,
                statement.keyspace(),
                statement.columnFamily(),
                "",
                errorMessage);
    }

    private Query extractGenericQuery(final long startTime, final long execTime, final CQLStatement statement,
            final QueryState queryState, final QueryOptions options, final String errorMessage) {
        return Query.create(
                startTime,
                execTime,
                queryState.getClientState().getRemoteAddress().toString(),
                Query.StatementType.UNKNOWN,
                "",
                "",
                "",
                errorMessage);
    }
}
