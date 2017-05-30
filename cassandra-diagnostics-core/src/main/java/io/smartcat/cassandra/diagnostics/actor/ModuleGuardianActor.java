package io.smartcat.cassandra.diagnostics.actor;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;

/**
 * Module actor guardian. Creates all module actors defined in configuration and takes care of their behavior and
 * life cycle.
 */
public class ModuleGuardianActor extends BaseActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private Router router = new Router(new BroadcastRoutingLogic());

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
