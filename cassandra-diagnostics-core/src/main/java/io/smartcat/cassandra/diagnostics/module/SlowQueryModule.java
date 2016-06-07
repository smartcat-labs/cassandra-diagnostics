package io.smartcat.cassandra.diagnostics.module;

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
import io.smartcat.cassandra.diagnostics.reporter.Reporter;

/**
 * Slow query module providing reports of query execution times over a defined threshold.
 */
public class SlowQueryModule extends Module {

    private static final Logger logger = LoggerFactory.getLogger(SlowQueryModule.class);

    private final String hostname;

    private final String service;

    /**
     * Constructor.
     *
     * @param configuration Module configuration
     * @param reporters     Reporter list
     */
    public SlowQueryModule(ModuleConfiguration configuration, List<Reporter> reporters) {
        super(configuration, reporters);
        hostname = getHostname();
        service = configuration.measurement;
    }

    @Override
    public void process(Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null");
        }

        if (hostname == null) {
            logger.error("Cannot log slow query because hostname is not resolved");
            return;
        }

        final Map<String, String> tags = new HashMap<>(2);
        tags.put("id", UUID.randomUUID().toString());
        tags.put("host", hostname);

        final Map<String, String> fields = new HashMap<>(4);
        fields.put("client", query.clientAddress());
        fields.put("statement", query.statement());
        fields.put("statementType", query.statementType().toString());
        fields.put("value", Long.toString(query.executionTimeInMilliseconds()));

        final Measurement measurement = Measurement
                .create(service, query.executionTimeInMilliseconds(), query.executionTimeInMilliseconds(),
                        TimeUnit.MILLISECONDS, tags, fields);

        logger.trace("Reporting measurement: {}", measurement);
        for (Reporter reporter : reporters) {
            reporter.report(measurement);
        }
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
