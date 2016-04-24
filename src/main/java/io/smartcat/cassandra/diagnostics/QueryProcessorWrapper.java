package io.smartcat.cassandra.diagnostics;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import com.google.inject.Inject;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.report.QueryReport;
import io.smartcat.cassandra.diagnostics.report.QueryReporter;

/**
 * This class is a Diagnostics wrapper for {@link QueryProcessor}. It reports CQL
 * queries that are executed slower than the configured execution threshold.
 */
public class QueryProcessorWrapper {

  /**
   * Class logger.
   */
  private static final Logger logger = LoggerFactory.getLogger(QueryProcessor.class);

  /**
   * The number of threads used for executing query reports.
   */
  private static final int EXECUTOR_NO_THREADS = 2;

  /**
   * Executor service used for executing query reports.
   */
  private static ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_NO_THREADS,
      new ThreadFactoryBuilder().setNameFormat("Cassandra-Diagnostics-%d").build());

  /**
   * Module configuration.
   */
  @Inject
  private Configuration config;

  /**
   * {@link QueryReport} used for query reporting.
   */
  @Inject
  private QueryReporter reporter;

  /**
   * Wrapper for {@link QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)} method.
   * This method implements the same functionality as the original method and, in addition, measures the
   * statement execution time and reports the query if it is larger than the configured execution time
   * threshold.
   *
   * @param statement {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @param queryState {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @param options {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @param origLogger internal class logger of {@link QueryProcessor}
   * @return {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @throws RequestExecutionException {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @throws RequestValidationException {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   */
  public ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options,
      Logger origLogger) throws RequestExecutionException, RequestValidationException {

    ResultMessage result;
    final long startTime = System.nanoTime();
    try {

      // original implementation
      origLogger.trace("Process {} @CL.{}", statement, options.getConsistency());
      ClientState clientState = queryState.getClientState();
      statement.checkAccess(clientState);
      statement.validate(clientState);

      result = statement.execute(queryState, options);
      // end of the original implementation

      final long execTime = System.nanoTime() - startTime;

      if (execTime >= config.slowQueryThreshold) {
        report(startTime, execTime, statement, queryState, options, null);
      }

      return result == null ? new ResultMessage.Void() : result;

    } catch (Exception err) {
      final long execTime = System.nanoTime() - startTime;
      report(startTime, execTime, statement, queryState, options, err.getMessage());
      throw err;
    }

  }

  /**
   * Submits a query reports asynchronously using the executor service.
   *
   * @param startTime execution start time, in nanoseconds
   * @param execTime execution time, in nanoseconds
   * @param statement CQL statement
   * @param queryState CQL query state
   * @param options CQL query options
   * @param errorMessage error message in case there was a problem during query execution
   */
  private void report(final long startTime, final long execTime, final CQLStatement statement,
      final QueryState queryState, final QueryOptions options, final String errorMessage) {

    // execute the rest of the action later, in a separate thread
    Future<Void> task = executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        reportExec(startTime, execTime, statement, queryState, options, errorMessage);
        return null;
      }
    });
    try {
      task.get();
    } catch (InterruptedException | ExecutionException e) {
      logger.error("An error occured while executing audit.", e);
    }
  }

  /**
   * Creates a query report based on the given information and pass it to the query reporter.
   *
   * @param startTime execution start time, in nanoseconds
   * @param execTime execution time, in nanoseconds
   * @param statement CQL statement
   * @param queryState CQL query state
   * @param options CQL query options
   * @param errorMessage error message in case there was a problem during query execution
   */
  private void reportExec(final long startTime, final long execTime, final CQLStatement statement,
      final QueryState queryState, final QueryOptions options, final String errorMessage) {
    QueryReport report = new QueryReport();
    report.startTime = startTime;
    report.executionTime = execTime;
    report.clientAddress = queryState.getClientState().getRemoteAddress().toString();
    report.statement = statement.getClass().getSimpleName();
    reporter.report(report);
  }

}
