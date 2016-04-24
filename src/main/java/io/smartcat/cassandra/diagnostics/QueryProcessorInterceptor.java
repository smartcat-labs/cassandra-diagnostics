package io.smartcat.cassandra.diagnostics;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;

import net.bytebuddy.implementation.bind.annotation.FieldValue;
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
   * Intercepter for {@link QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}.
   * Every invocation is being delegated to {@link QueryProcessorWrapper}.
   *
   * @param statement {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @param queryState {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @param options {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @param logger internal class logger of {@link QueryProcessor}
   * @return {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @throws RequestExecutionException {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   * @throws RequestValidationException {@see QueryProcessor#processStatement(CQLStatement, QueryState, QueryOptions)}
   */
  @RuntimeType
  public static ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options,
      @RuntimeType @FieldValue("logger") Logger logger) throws RequestExecutionException, RequestValidationException {

    return Diagnostics.queryProcessorWrapper.processStatement(statement, queryState, options, logger);

  }

}
