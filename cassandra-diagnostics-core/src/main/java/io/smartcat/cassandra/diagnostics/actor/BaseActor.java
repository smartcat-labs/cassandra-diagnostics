package io.smartcat.cassandra.diagnostics.actor;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Base actor class.
 */
public abstract class BaseActor extends AbstractActor {

    LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) {
        logger.error(reason, "Restarting due to [{}] when processing [{}]", reason.getMessage(),
                message.isPresent() ? message.get() : "");
    }

    @Override
    public void unhandled(Object message) {
        throw new RuntimeException("received unknown message");
    }

}
