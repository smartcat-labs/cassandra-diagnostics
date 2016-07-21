package io.smartcat.cassandra.diagnostics.connector;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;

import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.agent.builder.AgentBuilder.TypeStrategy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Connector implementation.
 */
public class ConnectorImpl implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorImpl.class);

    /**
     * {@link org.apache.cassandra.cql3.QueryProcessor} diagnostics wrapper.
     */
    private static QueryProcessorWrapper queryProcessorWrapper;

    private static CountDownLatch lock = new CountDownLatch(1);

    /**
     * {@link org.apache.cassandra.cql3.QueryProcessor} diagnostics wrapper getter.
     * @return QueryProcessorWrapper instance
     */
    public static QueryProcessorWrapper queryProcessorWrapper() {
        return queryProcessorWrapper;
    }

    /**
     * This method is supposed to be called from within the CassandraDaemon advice to
     * signal that Cassandra setup process is completed.
     */
    public static void cassandraSetupComplete() {
        lock.countDown();
    }

    /**
     * {@see io.smartcat.cassandra.diagnostics.connector.Connector#waitForSetupCompleted()}.
     */
    public void waitForSetupCompleted() {
        logger.info("Waiting for Cassandra setup process to complete.");
        try {
            lock.await();
            logger.info("Cassandra setup process completed.");
        } catch (InterruptedException e) {
            // This should never happen
            throw new IllegalStateException();
        }
    }

    /**
     * Initialize connector instance using the provided instrumentation.
     *
     * @param inst an Instrumentation reference
     * @param queryReporter QueryReporter implementation reference
     */
    public void init(Instrumentation inst, QueryReporter queryReporter) {
        queryProcessorWrapper = new QueryProcessorWrapper(queryReporter);
        setQueryProcessorIntercepter(inst);
        setCassandraDaemonIntercepter(inst);
    }

    /**
     * Installs intercepter for the QueryProcessor classes.
     *
     * @param inst instrumentation handle
     */
    private static void setQueryProcessorIntercepter(Instrumentation inst) {

        logger.info("Cassandra Diagnostics Connector: injecting org.apache.cassandra.cql3.QueryProcessor interceptor");

        final ElementMatcher.Junction<NamedElement> type = ElementMatchers
                .named("org.apache.cassandra.cql3.QueryProcessor");

        // Transformer for QueryProcessor
        final ByteBuddy byteBuddy = new ByteBuddy().with(Implementation.Context.Disabled.Factory.INSTANCE);
        new AgentBuilder.Default().with(byteBuddy)
                .with(RedefinitionStrategy.RETRANSFORMATION)
                .with(InitializationStrategy.NoOp.INSTANCE)
                .with(TypeStrategy.Default.REDEFINE)
                .type(type)
                .transform(new Transformer() {
                    @Override
                    public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription,
                            ClassLoader classLoader) {
                        return builder.visit(Advice.to(ProcessStatementAdvice.class)
                                        .on(named("processStatement")
                                            .and(takesArguments(cqlStatementDescription(),
                                                    queryStateDescription(), queryOptionsDescription()))
                                            .and(returns(
                                                    named("org.apache.cassandra.transport.messages.ResultMessage")))));
                    }
                })
                .installOn(inst);
    }

    /**
     * Installs intercepter for the CassandraDeamon classes.
     *
     * @param inst instrumentation handle
     */
    private static void setCassandraDaemonIntercepter(Instrumentation inst) {

        logger.info("Cassandra Diagnostics Connector: " +
                "injecting org.apache.cassandra.service.CassandraDaemon interceptor");

        final ElementMatcher.Junction<NamedElement> type = ElementMatchers
                .named("org.apache.cassandra.service.CassandraDaemon");

        // Transformer for QueryProcessor
        final ByteBuddy byteBuddy = new ByteBuddy().with(Implementation.Context.Disabled.Factory.INSTANCE);
        new AgentBuilder.Default().with(byteBuddy)
                .with(RedefinitionStrategy.RETRANSFORMATION)
                .with(InitializationStrategy.NoOp.INSTANCE)
                .with(TypeStrategy.Default.REDEFINE)
                .type(type)
                .transform(new Transformer() {
                    @Override
                    public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription,
                            ClassLoader classLoader) {
                        return builder.visit(Advice.to(CassandraDaemonAdvice.class)
                                        .on(named("completeSetup")));
                    }
                })
                .installOn(inst);
    }

    /**
     * QueryProcessor Advice.
     */
    public static class ProcessStatementAdvice {

        /**
         * Code executed before the intercepted method.
         * @return execution start time
         */
        @Advice.OnMethodEnter
        public static long enter() {
            final long startTime = System.currentTimeMillis();
            return startTime;
        }

        /**
         * Code executed after the intercepted method.
         *
         * @param startTime execution start time recorded by the enter method.
         * @param statement CQL statement to be executed
         * @param queryState query state information
         * @param options query options
         * @param result intercepted method's execution result
         */
        @Advice.OnMethodExit
        public static void exit(@Advice.Enter long startTime, @Advice.Argument(0) CQLStatement statement,
                @Advice.Argument(1) QueryState queryState, @Advice.Argument(2) QueryOptions options,
                @Advice.Return ResultMessage result) {
            ConnectorImpl.queryProcessorWrapper()
                .processStatement(statement, queryState, options, startTime, result, null);
        }
    }

    /**
     * Statement class type description helper.
     * @return CQLStatement class type description
     */
    private static TypeDescription cqlStatementDescription() {
        return new TypeDescription.Latent("org.apache.cassandra.cql3.CQLStatement",
                Modifier.INTERFACE, null, null);
    }

    /**
     * QueryState class type description helper.
     * @return QueryState class type description
     */
    private static TypeDescription queryStateDescription() {
        return new TypeDescription.Latent("org.apache.cassandra.service.QueryState",
                Modifier.PUBLIC | Modifier.ABSTRACT, null, null);
    }

    /**
     * QueryOptions class type description helper.
     * @return QueryOptions class type description
     */
    private static TypeDescription queryOptionsDescription() {
        return new TypeDescription.Latent("org.apache.cassandra.cql3.QueryOptions",
                Modifier.PUBLIC, null, null);
    }

    /**
     * CassandraDaemon advice.
     */
    public static class CassandraDaemonAdvice {
        /**
         * Code executed after the CassandraDaemon#completeSetup method.
         */
        @Advice.OnMethodExit
        public static void exit() {
            ConnectorImpl.cassandraSetupComplete();
        }
    }
}
