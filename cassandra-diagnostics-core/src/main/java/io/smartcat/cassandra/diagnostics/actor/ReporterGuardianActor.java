package io.smartcat.cassandra.diagnostics.actor;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Reporter actor guardian. Creates all reporter actors defined in configuration and takes care of their behavior and
 * life cycle.
 */
public class ReporterGuardianActor extends BaseActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private Router router = new Router(new BroadcastRoutingLogic());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Measurement.class, this::routeMeasurement)
                .match(Configuration.class, this::configure)
                .build();
    }

    private void configure(final Configuration configuration) {
        configuration.reporters.stream().forEach((reporterConfig) -> {
            try {
                ActorRef reporter = getContext().actorOf(ActorFactory.props(reporterConfig.reporter));
                reporter.tell(reporterConfig, self());

                getContext().watch(reporter);
                router.addRoutee(new ActorRefRoutee(reporter));
            } catch (Exception e) {
                logger.warning("Failed to create reporter by class name " + reporterConfig.reporter, e);
            }
        });
    }

    private void routeMeasurement(final Measurement measurement) {
        router.route(measurement, getSender());
    }
}
