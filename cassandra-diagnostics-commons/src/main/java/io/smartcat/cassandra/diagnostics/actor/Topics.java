package io.smartcat.cassandra.diagnostics.actor;

/**
 * Publish subscribe topics.
 */
public final class Topics {

    private Topics() {

    }

    /**
     * Query processing topic. All queries are pushed to this topic.
     */
    public static final String PROCESS_QUERY_TOPIC = "process-query-topic";

    /**
     * Cassandra setup completed topic. Message is pushed to this topic when cassandra has started.
     */
    public static final String CASSANDRA_SETUP_COMPLETED_TOPIC = "cassandra-setup-completed-topic";

    /**
     * Info provider topic. Used to query node probe.
     */
    public static final String INFO_PROVIDER_TOPIC = "info-provider-topic";

}
