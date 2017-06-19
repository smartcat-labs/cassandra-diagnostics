package io.smartcat.cassandra.diagnostics.actor;

import akka.actor.ActorRef;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import io.smartcat.cassandra.diagnostics.actor.messages.Command;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Reporter actor guardian. Creates all reporter actors defined in configuration and takes care of their behavior and
 * life cycle.
 */
public class ReporterGuardianActor extends BaseActor {

    private final Configuration configuration;

    private Router router = new Router(new BroadcastRoutingLogic());

    /**
     * Constructor.
     *
     * @param configuration Configuration
     */
    public ReporterGuardianActor(final Configuration configuration) {
        this.configuration = configuration;

        this.configuration.reporters.stream().forEach((reporterConfig) -> {
            try {
                ActorRef reporter = getContext()
                        .actorOf(ActorFactory.reporterProps(reporterConfig.reporter, configuration));
                getContext().watch(reporter);
                router = router.addRoutee(new ActorRefRoutee(reporter));
            } catch (Exception e) {
                logger.warning("Failed to create reporter by class name " + reporterConfig.reporter, e);
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
