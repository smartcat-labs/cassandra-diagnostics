package io.smartcat.cassandra.diagnostics.connector;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;

import io.smartcat.cassandra.diagnostics.config.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.info.InfoProviderActor;
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

    private static ExecuteStatementWrapper executeStatementWrapper;

    /**
     * Driver session manager diagnostics wrapper.
     *
     * @return ExecuteStatementWrapper instance
     */
    public static ExecuteStatementWrapper executeStatementWrapper() {
        return executeStatementWrapper;
    }

    /**
     * Initialize connector instance using the provided instrumentation.
     *
     * @param inst                  Instrumentation reference
     * @param queryReporter         QueryReporter implementation reference
     * @param configuration         Connector configuration
     * @param globalConfiguration   global configuration general for diagnostics
     */
    public void init(Instrumentation inst, QueryReporter queryReporter, ConnectorConfiguration configuration,
            GlobalConfiguration globalConfiguration) {
        executeStatementWrapper = new ExecuteStatementWrapper(queryReporter, configuration, globalConfiguration);
        setIntercepters(inst);
    }

    /**
     * {@link io.smartcat.cassandra.diagnostics.connector.Connector#waitForSetupCompleted()}.
     */
    public void waitForSetupCompleted() {
        logger.info("Waiting for the driver setup process to complete.");
        logger.info("The driver setup process completed.");
    }

    /**
     * Get an InfoProviderActor implementation providing cassandra status information.
     *
     * @return {@code io.smartcat.cassandra.diagnostics.info.InfoProviderActor} implementation.
     */
    public InfoProviderActor getInfoProvider() {
        return null;
    }

    /**
     * Installs intercepter for the target classes.
     *
     * @param inst instrumentation handle
     */
    private static void setIntercepters(Instrumentation inst) {

        logger.info("Cassandra Diagnostics Connector: injecting " +
                "com.datastax.driver.core.SessionManager#executeAsync interceptor");

        final ElementMatcher.Junction<NamedElement> type = ElementMatchers
                .named("com.datastax.driver.core.SessionManager");

        // Transformer for SessionManager
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
                        return builder.visit(Advice.to(ExecuteStatementAdvice.class)
                                        .on(named("executeAsync")
                                            .and(takesArguments(statementDescription()))
                                            .and(returns(named("com.datastax.driver.core.ResultSetFuture")))));
                    }
                })
                .installOn(inst);

    }

    /**
     * ByteBuddy Advice.
     */
    public static class ExecuteStatementAdvice {

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
         * @param result CQL result future
         */
        @Advice.OnMethodExit
        public static void exit(@Advice.Enter long startTime, @Advice.Argument(0) Statement statement,
                @Advice.Return ResultSetFuture result) {
            ConnectorImpl.executeStatementWrapper().processStatement(statement, startTime, result);
        }
    }

    /**
     * Statement class type description helper.
     * @return Statement class type description
     */
    private static TypeDescription statementDescription() {
        return new TypeDescription.Latent("com.datastax.driver.core.Statement",
                Modifier.ABSTRACT, null, null);
    }

}
