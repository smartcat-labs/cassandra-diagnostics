package io.smartcat.cassandra.diagnostics.actor;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Module actor guardian. Creates all module actors defined in configuration and takes care of their behavior and
 * life cycle.
 */
public class ModuleGuardianActor extends BaseActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private Router router = new Router(new BroadcastRoutingLogic());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Configuration.class, this::configure)
                .build();
    }

    private void configure(final Configuration configuration) {
        configuration.modules.stream().forEach((moduleConfiguration) -> {
            try {
                ActorRef module = getContext().actorOf(ActorFactory.props(moduleConfiguration.module));
                module.tell(moduleConfiguration, self());

                getContext().watch(module);
                router.addRoutee(new ActorRefRoutee(module));
            } catch (Exception e) {
                logger.warning("Failed to create module by class name " + moduleConfiguration.module, e);
            }
        });
    }
}
