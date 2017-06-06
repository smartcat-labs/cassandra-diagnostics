package io.smartcat.cassandra.diagnostics.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Node guardian actor class.
 */
public class NodeGuardianActor extends ClusterAware {

    private final ActorRef reporterGuardianActor;

    private final ActorRef moduleGuardianActor;

    /**
     * Constructor.
     */
    public NodeGuardianActor() {
        reporterGuardianActor = getContext().actorOf(Props.create(ReporterGuardianActor.class));
        getContext().watch(reporterGuardianActor);

        moduleGuardianActor = getContext().actorOf(Props.create(ModuleGuardianActor.class));
        getContext().watch(moduleGuardianActor);

        logger.debug("NodeGuardian created");
    }

    /**
     * Build actor's receive pattern.
     *
     * @return receive pattern
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Configuration.class, this::configure)
                .match(Messages.Start.class, this::start)
                .match(Messages.Stop.class, this::stop)
                .match(Messages.GracefulShutdown.class, o -> gracefulShutdown(getSender()))
                .build();
    }

    private void configure(Configuration configuration) {
        reporterGuardianActor.tell(configuration, getSelf());
        moduleGuardianActor.tell(configuration, getSelf());
    }

    private void start(Messages.Start start) {
//        reporterGuardianActor.tell(start, getSelf());
        moduleGuardianActor.tell(start, getSelf());
    }

    private void stop(Messages.Stop stop) {
//        reporterGuardianActor.tell(stop, getSelf());
        moduleGuardianActor.tell(stop, getSelf());
    }

    private void gracefulShutdown(ActorRef listener) {

    }
}
