package io.smartcat.cassandra.diagnostics.actor;

import java.lang.reflect.Constructor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;

/**
 * Actor factory class.
 */
public class ActorFactory {

    private ActorFactory() {

    }

    /**
     * Create actor props from class name without any parameters.
     *
     * @param className Class name
     * @param <T>       Actor type
     * @return T Actor instance
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public static <T extends AbstractActor> Props props(final String className)
            throws ClassNotFoundException, NoSuchMethodException {
        Constructor<?> constructor = Class.forName(className).getConstructor();
        return Props.create(new Creator<T>() {
            @Override
            public T create() throws Exception {
                return (T) constructor.newInstance();
            }
        });
    }

}
