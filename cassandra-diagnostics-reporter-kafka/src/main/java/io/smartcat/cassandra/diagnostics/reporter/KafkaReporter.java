package io.smartcat.cassandra.diagnostics.reporter;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * Apache Kafka based {@link Reporter} implementation. All measurements are written into Kafka based on measurements
 * name and time of the measurement.
 */
public class KafkaReporter extends Reporter {
    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(KafkaReporter.class);

    private static final String SERVERS_PROP = "kafkaBootstrapServers";
    private static final String TOPIC_PROP = "kafkaTopic";

    private static Producer<String, String> producer;

    private String partitionKey;
    private String topic;

    /**
     * Constructor.
     *
     * @param configuration        Reporter configuration
     * @param globalConfiguration  Global diagnostics configuration
     */
    public KafkaReporter(ReporterConfiguration configuration, GlobalConfiguration globalConfiguration) {
        super(configuration, globalConfiguration);

        final String servers = configuration.getDefaultOption(SERVERS_PROP, "");
        if (servers.isEmpty()) {
            logger.warn("Missing required property " + SERVERS_PROP + ". Aborting initialization.");
            return;
        }

        topic = configuration.getDefaultOption(TOPIC_PROP, "");
        if (topic.isEmpty()) {
            logger.warn("Missing required property " + TOPIC_PROP + ". Aborting initialization.");
            return;
        }

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(properties);
        partitionKey = globalConfiguration.systemName + "_" + globalConfiguration.hostname;
    }

    @Override
    public void report(Measurement measurement) {
        if (producer == null) {
            logger.warn("Kafka producer is not initialized.");
            return;
        }

        producer.send(new ProducerRecord<>(topic, partitionKey, measurement.toJson()));
    }

    @Override
    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }
}
