package io.smartcat.cassandra.diagnostics.module;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.smartcat.cassandra.diagnostics.config.Configuration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.ReporterActor;

public class LatchTestReporter extends ReporterActor {

    private CountDownLatch latch;

    private final List<Measurement> reported = new ArrayList<>();

    public LatchTestReporter(Configuration configuration, CountDownLatch latch) {
        super("latch-test-reporter", configuration);
        this.latch = latch;
    }

    @Override
    public void report(Measurement measurement) {
        reported.add(measurement);
        System.out.println(measurement.toString());
        latch.countDown();
    }

    /**
     * Prevent concurrency issues and return always copy of reported values.
     *
     * @return copy of all reported values.
     */
    public List<Measurement> getReported() {
        return new ArrayList<>(reported);
    }

}
