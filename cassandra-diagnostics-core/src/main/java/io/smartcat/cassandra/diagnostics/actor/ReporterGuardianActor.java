package io.smartcat.cassandra.diagnostics.actor;

import akka.actor.ActorRef;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Reporter actor guardian. Creates all reporter actors defined in configuration and takes care of their behavior and
 * life cycle.
 */
public class ReporterGuardianActor extends BaseActor {

    private Router router = new Router(new BroadcastRoutingLogic());

    /**
     * Build actor's receive pattern.
     *
     * @return receive pattern
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Configuration.class, this::configure)
                .build();
    }

    private void configure(final Configuration configuration) {
        logger.debug("Received configuration");
        configuration.reporters.stream().forEach((reporterConfig) -> {
            try {
                ActorRef reporter = getContext().actorOf(ReporterActor.props(reporterConfig));
                getContext().watch(reporter);
                router = router.addRoutee(new ActorRefRoutee(reporter));
            } catch (Exception e) {
                logger.warning("Failed to create reporter by class name " + reporterConfig.reporter, e);
            }
        });
    }

}
