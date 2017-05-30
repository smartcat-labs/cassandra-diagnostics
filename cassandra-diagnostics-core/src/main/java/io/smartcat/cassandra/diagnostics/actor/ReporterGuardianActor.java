package io.smartcat.cassandra.diagnostics.actor;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * Reporter actor guardian. Creates all reporter actors defined in configuration and takes care of their behavior and
 * life cycle.
 */
public class ReporterGuardianActor extends BaseActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private Router router = new Router(new BroadcastRoutingLogic());

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Measurement.class, this::routeMeasurement).build();
    }

    private void configure() {
//        config.reporters.stream().forEach((reporterConfig -> {
//        ActorRef reporter = getContext().actorOf(ActorFactory.props(reporterConfig.name));
//        getContext().watch(reporter);
//        router.addRoutee(new ActorRefRoutee(reporter));
//        }));
    }

    private void routeMeasurement(final Measurement measurement) {
        router.route(measurement, getSender());
    }
}
