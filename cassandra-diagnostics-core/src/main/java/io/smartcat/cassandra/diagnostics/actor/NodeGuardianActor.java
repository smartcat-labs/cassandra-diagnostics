package io.smartcat.cassandra.diagnostics.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Node guardian actor class.
 */
public class NodeGuardianActor extends BaseActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final ActorRef moduleGuardianActor;

    private final ActorRef reporterGuardianActor;

    public NodeGuardianActor() {
        moduleGuardianActor = getContext().actorOf(Props.create(ModuleGuardianActor.class));
        getContext().watch(moduleGuardianActor);

        reporterGuardianActor = getContext().actorOf(Props.create(ReporterGuardianActor.class));
        getContext().watch(reporterGuardianActor);

        logger.debug("NodeGuardian created");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GracefulShutdown.class, o -> gracefulShutdown(sender())).build();
    }

    protected void gracefulShutdown(ActorRef listener) {

    }
}
