package io.smartcat.cassandra.diagnostics.actor;

import akka.actor.ActorRef;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Node guardian actor class.
 */
public class NodeGuardianActor extends ClusterAware {

    private final Configuration configuration;

    private final ActorRef reporterGuardianActor;

    private final ActorRef moduleGuardianActor;

    private final ActorRef connector;

    /**
     * Constructor.
     *
     * @param configuration Configuration
     */
    public NodeGuardianActor(final Configuration configuration) {
        this.configuration = configuration;

        this.reporterGuardianActor = getContext()
                .actorOf(ActorFactory.props(ReporterGuardianActor.class, configuration));
        getContext().watch(reporterGuardianActor);

        this.moduleGuardianActor = getContext().actorOf(ActorFactory.props(ModuleGuardianActor.class, configuration));
        getContext().watch(moduleGuardianActor);

        this.connector = getContext()
                .actorOf(ActorFactory.connectorProps(configuration.connector.connector, configuration));
        getContext().watch(connector);

        logger.debug("NodeGuardian created");
    }

    /**
     * Build actor's receive pattern.
     *
     * @return receive pattern
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Command.Start.class, this::start).match(Command.Stop.class, this::stop)
                .match(Command.GracefulShutdown.class, this::gracefulShutdown).build();
    }

    private void start(Command.Start start) {
        reporterGuardianActor.tell(start, getSelf());
        moduleGuardianActor.tell(start, getSelf());
    }

    private void stop(Command.Stop stop) {
        reporterGuardianActor.tell(stop, getSelf());
        moduleGuardianActor.tell(stop, getSelf());
    }

    private void gracefulShutdown(Command.GracefulShutdown shutdown) {

    }
}
