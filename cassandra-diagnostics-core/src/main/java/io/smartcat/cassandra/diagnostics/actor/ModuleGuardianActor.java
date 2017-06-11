package io.smartcat.cassandra.diagnostics.actor;

import akka.actor.ActorRef;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Module actor guardian. Creates all module actors defined in configuration and takes care of their behavior and
 * life cycle.
 */
public class ModuleGuardianActor extends BaseActor {

    private final Configuration configuration;

    private Router router = new Router(new BroadcastRoutingLogic());

    /**
     * Constructor.
     *
     * @param configuration Configuration
     */
    public ModuleGuardianActor(final Configuration configuration) {
        this.configuration = configuration;

        this.configuration.modules.stream().forEach((moduleConfiguration) -> {
            try {
                ActorRef module = getContext()
                        .actorOf(ActorFactory.moduleProps(moduleConfiguration.module, configuration));
                getContext().watch(module);
                router = router.addRoutee(new ActorRefRoutee(module));
            } catch (Exception e) {
                logger.warning("Failed to create module by class name " + moduleConfiguration.module, e);
            }
        });
    }

    /**
     * Build actor's receive pattern.
     *
     * @return receive pattern
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Command.Start.class, o -> router.route(o, getSelf()))
                .match(Command.Stop.class, o -> router.route(o, getSelf()))
                .build();
    }

}
