package io.smartcat.cassandra.diagnostics.connector;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.statements.ModificationStatement;
import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Query;

/**
 * This class is a Diagnostics wrapper for {@link org.apache.cassandra.cql3.QueryProcessor}.
 */
public class QueryProcessorWrapper extends AbstractEventProcessor {

    private static final Logger logger = LoggerFactory.getLogger(QueryProcessorWrapper.class);

    /**
     * Constructor.
     *
     * @param queryReporter QueryReporter used to report queries
     * @param configuration Connector configuration
     */
    public QueryProcessorWrapper(QueryReporter queryReporter, ConnectorConfiguration configuration) {
        super(queryReporter, configuration);
    }

    /**
     * Wrapper for
     * {@link org.apache.cassandra.cql3.QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)} method.
     * This method is invoked after the original method, measures the execution time and reports query.
     *
     * @param statement  QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param queryState QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param options    QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)
     * @param startTime query execution start time
     * @param result the original method result
     * @param error the original method execution's error (if any)
     */
    public void processStatement(CQLStatement statement, QueryState queryState, QueryOptions options,
            long startTime, ResultMessage result, Throwable error) {

        final long execTime = System.currentTimeMillis() - startTime;
        final String errorMessage = error != null ? error.getMessage() : null;
        report(startTime, execTime, statement, queryState, options, errorMessage);
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
        report(new Runnable() {
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
