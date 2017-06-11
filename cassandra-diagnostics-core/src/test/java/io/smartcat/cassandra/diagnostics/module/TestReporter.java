package io.smartcat.cassandra.diagnostics.module;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.ReporterActor;

/**
 * Test reporter class.
 */
public class TestReporter extends ReporterActor {

    private ActorRef target = null;

    public static final class QueryReports {

    }

    public final List<Measurement> reported = new ArrayList<Measurement>();

    /**
     * Constructor.
     *
     * @param configuration reporter configuration
     */
    public TestReporter(final String reporterName, final Configuration configuration) {
        super(reporterName, configuration);
    }

    @Override
    public Receive createReceive() {
        return defaultReceive().match(ActorRef.class, actorRef -> {
            target = actorRef;
            getSender().tell("done", getSelf());
        }).match(QueryReports.class, o -> {
            getSender().tell(reported, getSelf());
        }).build();
    }

    @Override
    protected void report(Measurement measurement) {
        if (target != null) target.tell(measurement, getSelf());
        reported.add(measurement);
    }

}
