package io.smartcat.cassandra.diagnostics.module.requestrate;

import java.util.List;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Request rate module providing request rates at defined intervals. Request rates can be total or separate for read
 * and write.
 */
public class RequestRateModule extends Module {

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     */
    public RequestRateModule(ModuleConfiguration configuration, List<Reporter> reporters) {
        super(configuration, reporters);
    }

    @Override
    public Measurement transform(Query query) {
        return null;
    }
}
