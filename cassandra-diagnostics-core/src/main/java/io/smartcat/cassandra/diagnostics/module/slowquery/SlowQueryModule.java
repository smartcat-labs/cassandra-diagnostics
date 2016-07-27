package io.smartcat.cassandra.diagnostics.module.slowquery;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.config.ConfigurationException;
import io.smartcat.cassandra.diagnostics.module.Module;
import io.smartcat.cassandra.diagnostics.module.ModuleConfiguration;
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Slow query module providing reports of query execution times over a defined threshold.
 */
public class SlowQueryModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(SlowQueryModule.class);

    private final String hostname;

    private final String service;

    private final SlowQueryLogDecider slowQueryLogDecider;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     * @throws ConfigurationException in case the provided module configuration is not valid
     */
    public SlowQueryModule(ModuleConfiguration configuration, List<Reporter> reporters) throws ConfigurationException {
        super(configuration, reporters);
        hostname = getHostname();
        service = configuration.measurement;
        slowQueryLogDecider = SlowQueryLogDecider.create(SlowQueryConfiguration.create(configuration.options));
    }

    @Override
    protected boolean isForReporting(Query query) {
        return slowQueryLogDecider.isForReporting(query);
    }

    @Override
    public Measurement transform(Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null");
        }

        if (hostname == null) {
            logger.error("Cannot log slow query because hostname is not resolved");
            throw new IllegalArgumentException("Cannot log slow query because hostname is not resolved.");
        }

        final Map<String, String> tags = new HashMap<>(4);
        tags.put("id", UUID.randomUUID().toString());
        tags.put("host", hostname);
        tags.put("statementType", query.statementType().toString());

        final Map<String, String> fields = new HashMap<>(4);
        fields.put("client", query.clientAddress());
        fields.put("statement", query.statement());

        final Measurement measurement = Measurement
                .create(service, query.executionTimeInMilliseconds(), query.startTimeInMilliseconds(),
                        TimeUnit.MILLISECONDS, tags, fields);

        logger.trace("Measurement transformed: {}", measurement);
        return measurement;
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Cannot resolve local host hostname");
            return null;
        }
    }
}
