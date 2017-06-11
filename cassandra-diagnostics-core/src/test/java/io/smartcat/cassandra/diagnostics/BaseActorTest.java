package io.smartcat.cassandra.diagnostics;

import org.junit.After;
import org.junit.Before;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

/**
 * Base actor test class.
 */
public abstract class BaseActorTest {

    protected ActorSystem system = null;

    @Before
    public void setup() {
        system = ActorSystem.create();
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

}
