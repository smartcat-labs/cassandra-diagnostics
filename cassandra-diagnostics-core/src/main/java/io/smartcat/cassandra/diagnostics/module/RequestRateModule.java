package io.smartcat.cassandra.diagnostics.module;

import io.smartcat.cassandra.diagnostics.Query;

/**
 * Request rate module providing request rates at defined intervals. Request rates can be total or separate for read
 * and write.
 */
public class RequestRateModule extends Module {

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     */
    public RequestRateModule(ModuleConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void process(Query query) {

    }
}
