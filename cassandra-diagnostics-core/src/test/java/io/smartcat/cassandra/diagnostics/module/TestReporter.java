package io.smartcat.cassandra.diagnostics.module;

import java.util.ArrayList;
import java.util.List;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

/**
 * Test reporter class.
 */
public class TestReporter extends Reporter {

    public final List<Measurement> reported = new ArrayList<Measurement>();

    public TestReporter(ReporterConfiguration configuration, GlobalConfiguration globalConfiguration) {
        super(configuration, globalConfiguration);
    }

    @Override
    public void report(Measurement measurement) {
        reported.add(measurement);
    }

}
