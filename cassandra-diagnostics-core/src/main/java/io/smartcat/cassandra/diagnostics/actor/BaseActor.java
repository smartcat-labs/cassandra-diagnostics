package io.smartcat.cassandra.diagnostics.actor;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Base actor class.
 */
public abstract class BaseActor extends AbstractActor {

    protected LoggingAdapter logger = Logging.getLogger(getContext().getSystem().eventStream(), this);

    /**
     * Method executed prior to restart.
     *
     * @param reason  restart reason
     * @param message restart message
     */
    @Override
    public void preRestart(Throwable reason, Optional<Object> message) {
        logger.error(reason, "Restarting due to [{}] when processing [{}]", reason.getMessage(),
                message.isPresent() ? message.get() : "");
    }

    /**
     * Unhandled message type handling method throwing runtime exception enforcing that all messages should be handled.
     *
     * @param message unhandled message
     */
    @Override
    public void unhandled(Object message) {
        throw new RuntimeException("Received unknown message: " + message.toString());
    }

}
