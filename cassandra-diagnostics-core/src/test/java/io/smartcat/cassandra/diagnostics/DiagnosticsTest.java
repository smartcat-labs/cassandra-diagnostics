package io.smartcat.cassandra.diagnostics;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.config.Configuration;

public class DiagnosticsTest {

    @Test
    public void test_diagnostics_reload() {
        Diagnostics diagnostics = new Diagnostics();
        diagnostics.activate();
        Configuration configuration = diagnostics.getConfiguration();
        diagnostics.reload();
    }

}
