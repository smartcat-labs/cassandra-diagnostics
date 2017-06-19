package io.smartcat.cassandra.diagnostics.actor;

import java.lang.reflect.Constructor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.connector.ConnectorActor;
import io.smartcat.cassandra.diagnostics.connector.ConnectorConfiguration;
import io.smartcat.cassandra.diagnostics.info.InfoProviderActor;
import io.smartcat.cassandra.diagnostics.module.ModuleActor;
import io.smartcat.cassandra.diagnostics.reporter.ReporterActor;

/**
 * Actor factory class.
 */
public class ActorFactory {

    private ActorFactory() {

    }

    /**
     * * Create actor props from class type without {@code Configuration} parameter.
     *
     * @param type          Class type
     * @param configuration Configuration
     * @param <T>           Class type
     * @return Actor instance
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public static <T extends AbstractActor> Props props(final Class<T> type, final Configuration configuration) {
        try {
            Constructor<?> constructor = type.getConstructor(Configuration.class);
            return Props.create(new Creator<T>() {
                @Override
                public T create() throws Exception {
                    return (T) constructor.newInstance(configuration);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize actor for type: " + type.getName(), e);
        }
    }

    /**
     * Create actor props from reporter actor class name.
     *
     * @param className     Reporter actor class name
     * @param configuration configuration
     * @return Reporter actor instance
     * @throws ClassNotFoundException no class found
     * @throws NoSuchMethodException  no such method
     */
    public static Props reporterProps(final String className, final Configuration configuration)
            throws ClassNotFoundException, NoSuchMethodException {
        Constructor<?> constructor = Class.forName(className).getConstructor(String.class, Configuration.class);
        return Props.create(ReporterActor.class, new Creator<ReporterActor>() {
            @Override
            public ReporterActor create() throws Exception {
                return (ReporterActor) constructor.newInstance(className, configuration);
            }
        });
    }

    /**
     * Create actor props from module actor class name.
     *
     * @param className     Module actor class name
     * @param configuration configuration
     * @return Module actor instance
     * @throws ClassNotFoundException no class found
     * @throws NoSuchMethodException  no such method
     */
    public static Props moduleProps(final String className, final Configuration configuration)
            throws ClassNotFoundException, NoSuchMethodException {
        Constructor<?> constructor = Class.forName(className).getConstructor(String.class, Configuration.class);
        return Props.create(ModuleActor.class, new Creator<ModuleActor>() {
            @Override
            public ModuleActor create() throws Exception {
                return (ModuleActor) constructor.newInstance(className, configuration);
            }
        });
    }

    /**
     * Create actor props from connector actor class name.
     *
     * @param className     Connector actor implementation class name
     * @param configuration configuration
     * @return Connector actor instance
     * @throws NoSuchMethodException no such method
     */
    public static Props connectorProps(final String className, final Configuration configuration) {
        try {
            Constructor<?> constructor = Class.forName(className).getConstructor(Configuration.class);
            return Props.create(ConnectorActor.class, new Creator<ConnectorActor>() {
                @Override
                public ConnectorActor create() throws Exception {
                    return (ConnectorActor) constructor.newInstance(configuration);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize connector props for class name: " + className, e);
        }
    }

    /**
     * Create actor props for info provider actor.
     *
     * @param type          Info provider actor implementation class
     * @param configuration configuration
     * @param <T>           class type
     * @return Info provider actor instance
     * @throws NoSuchMethodException no such method
     */
    public static <T extends InfoProviderActor> Props infoProviderProps(final Class<T> type,
            final ConnectorConfiguration configuration) throws NoSuchMethodException {
        Constructor<?> constructor = type.getConstructor(ConnectorConfiguration.class);
        return Props.create(new Creator<T>() {
            @Override
            public T create() throws Exception {
                return (T) constructor.newInstance(configuration);
            }
        });
    }

}
