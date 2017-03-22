package io.smartcat.cassandra.diagnostics.connector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.statements.ModificationStatement;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.MD5Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import io.smartcat.cassandra.diagnostics.Query;

/**
 * This class is a Diagnostics wrapper for {@link org.apache.cassandra.cql3.QueryProcessor}.
 */
public class QueryProcessorWrapper extends AbstractEventProcessor {

    private static final Logger logger = LoggerFactory.getLogger(QueryProcessorWrapper.class);

    private static final ConcurrentMap<MD5Digest, String> preparedStatementQueries = new ConcurrentHashMap<>();

    private boolean slowQueryTracingEnabled = false;

    /**
     * Constructor.
     *
     * @param queryReporter QueryReporter used to report queries
     * @param configuration Connector configuration
     */
    public QueryProcessorWrapper(QueryReporter queryReporter, ConnectorConfiguration configuration) {
        super(queryReporter, configuration);
        slowQueryTracingEnabled = configuration.enableTracing;
    }

    /**
     * Wrapper for
     * {@link org.apache.cassandra.cql3.QueryProcessor#processPrepared(CQLStatement, QueryState, QueryOptions)} method.
     * This method is invoked after the original method, measures the execution time and reports query.
     *
     * @param statement  QueryProcessor#processPrepared(CQLStatement, QueryState, QueryOptions)
     * @param queryState QueryProcessor#processPrepared(CQLStatement, QueryState, QueryOptions)
     * @param options    QueryProcessor#processPrepared(CQLStatement, QueryState, QueryOptions)
     * @param startTime  query execution start time
     */
    public void processPrepared(CQLStatement statement, QueryState queryState, QueryOptions options, long startTime,
            ResultMessage result, ConcurrentLinkedHashMap<MD5Digest, ParsedStatement.Prepared> preparedStatements) {
        final long execTime = System.currentTimeMillis() - startTime;
        report(startTime, execTime, null, statement, queryState, options, preparedStatements);
    }

    /**
     * Wrapper for
     * {@link org.apache.cassandra.cql3.QueryProcessor#process(String, QueryState, QueryOptions)} method.
     * This method is invoked after the original method, measures the execution time and reports query.
     *
     * @param queryString  QueryProcessor#process(String, QueryState, QueryOptions)
     * @param queryState QueryProcessor#process(String, QueryState, QueryOptions)
     * @param options    QueryProcessor#process(String, QueryState, QueryOptions)
     * @param startTime  query execution start time
     */
    public void process(String queryString, QueryState queryState, QueryOptions options, long startTime,
            ResultMessage result) {
        final long execTime = System.currentTimeMillis() - startTime;
        report(startTime, execTime, queryString, null, queryState, options, null);
    }

    /**
     * Wrapper for
     * {@link org.apache.cassandra.cql3.QueryProcessor#storePreparedStatement(String, String,
     * ParsedStatement.Prepared, boolean)} method.
     * This method is invoked after the original method, measures the execution time and reports query.
     *
     * @param queryString QueryProcessor#storePreparedStatement(String, String, ParsedStatement.Prepared, boolean)
     * @param keyspace    QueryProcessor#storePreparedStatement(String, String, ParsedStatement.Prepared, boolean)
     * @param forThrift   QueryProcessor#storePreparedStatement(String, String, ParsedStatement.Prepared, boolean)
     * @param prepared    QueryProcessor#storePreparedStatement(String, String, ParsedStatement.Prepared, boolean)
     * @param preparedStatements  QueryProcessor#preparedStatements
     */
    public void storePrepared(String queryString, String keyspace, boolean forThrift,
            ParsedStatement.Prepared prepared,
            ConcurrentLinkedHashMap<MD5Digest, ParsedStatement.Prepared> preparedStatements) {
        for (MD5Digest digest : preparedStatements.keySet()) {
            ParsedStatement.Prepared existingPrepared = preparedStatements.get(digest);
            if (prepared == existingPrepared) {
                preparedStatementQueries.put(digest, queryString);
            }
        }
    }

    /**
     * Submits a query reports asynchronously.
     *
     * @param startTime  execution start time, in milliseconds
     * @param execTime   execution time, in milliseconds
     * @param preparedStatementKey  prepared statement digest/key
     * @param queryState CQL query state
     * @param options    CQL query options
     */
    private void report(final long startTime, final long execTime, final String queryString,
            final CQLStatement statement, final QueryState queryState, final QueryOptions options,
            final ConcurrentLinkedHashMap<MD5Digest, ParsedStatement.Prepared> preparedStatements) {
        if (queryState.getClientState().isInternal) {
            return;
        }
        report(new Runnable() {
            @Override
            public void run() {
                try {
                    CQLStatement cqlStatement;
                    String cqlQuery = "";

                    if (statement == null && queryString == null) {
                        throw new IllegalStateException("Both prepared statement and query string are missing.");
                    } else if (statement == null) {
                        cqlStatement = QueryProcessor.parseStatement(queryString).prepare().statement;
                        cqlQuery = queryString;
                    } else {
                        cqlStatement = statement;

                        if (slowQueryTracingEnabled) {
                            MD5Digest preparedStatementKey = null;
                            for (MD5Digest digest : preparedStatements.keySet()) {
                                ParsedStatement.Prepared prepared = preparedStatements.get(digest);
                                if (prepared.statement == statement) {
                                    preparedStatementKey = digest;
                                }
                            }
                            cqlQuery = preparedStatementQueries.get(preparedStatementKey);
                        }
                    }

                    Query query = createQuery(startTime, execTime, cqlQuery, cqlStatement, queryState, options);
                    logger.trace("Reporting query: {}.", query);
                    queryReporter.report(query);
                } catch (Exception e) {
                    logger.warn("An error occured while reporting query", e);
                }
            }
        });
    }

    private Query createQuery(final long startTime, final long execTime, final String queryString,
            final CQLStatement statement, final QueryState queryState, final QueryOptions options) {
        Query query;
        if (statement instanceof SelectStatement) {
            query = createQuery(startTime, execTime, queryString, (SelectStatement) statement,
                    queryState, options);
        } else if (statement instanceof ModificationStatement) {
            query = createQuery(startTime, execTime, queryString, (ModificationStatement) statement,
                    queryState, options);
        } else {
            query = createGenericQuery(startTime, execTime, queryString, statement, queryState, options);
        }
        return query;
    }

    private Query createQuery(final long startTime, final long execTime, final String queryString,
            final SelectStatement statement, final QueryState queryState, final QueryOptions options) {
        return Query.create(startTime, execTime, queryState.getClientState().getRemoteAddress().toString(),
                Query.StatementType.SELECT, statement.keyspace(), statement.columnFamily(), queryString);
    }

    private Query createQuery(final long startTime, final long execTime, final String queryString,
            final ModificationStatement statement, final QueryState queryState, final QueryOptions options) {
        return Query.create(startTime, execTime, queryState.getClientState().getRemoteAddress().toString(),
                Query.StatementType.UPDATE, statement.keyspace(), statement.columnFamily(), queryString);
    }

    private Query createGenericQuery(final long startTime, final long execTime, final String queryString,
            final CQLStatement statement, final QueryState queryState, final QueryOptions options) {
        return Query.create(startTime, execTime, queryState.getClientState().getRemoteAddress().toString(),
                Query.StatementType.UNKNOWN, "", "", queryString);
    }
}
