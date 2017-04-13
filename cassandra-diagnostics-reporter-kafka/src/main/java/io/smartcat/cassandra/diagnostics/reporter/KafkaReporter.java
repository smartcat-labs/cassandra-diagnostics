package io.smartcat.cassandra.diagnostics.reporter;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.utils.Utils;

/**
 * Apache Kafka based {@link Reporter} implementation. All measurements are written into Kafka based on measurements
 * name and time of the measurement.
 */
public class KafkaReporter extends Reporter {
    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(KafkaReporter.class);

    private static final String SERVERS = "kafkaBootstrapServers";

    private static Producer<String, String> producer;

    private String partitionKey;

    /**
     * Constructor.
     *
     * @param configuration Reporter configuration
     */
    public KafkaReporter(ReporterConfiguration configuration) {
        super(configuration);

        final String servers = configuration.getDefaultOption(SERVERS, "");
        if (servers.isEmpty()) {
            logger.warn("Missing required property " + SERVERS + ". Aborting initialization.");
            return;
        }

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(properties);
        partitionKey = Utils.getSystemname() + "_" + Utils.getHostname();
    }

    @Override
    public void report(Measurement measurement) {
        if (producer == null) {
            logger.warn("Kafka producer is not initialized.");
            return;
        }

        producer.send(new ProducerRecord<>(measurement.name(), partitionKey, measurement.toJson()));
    }

    @Override
    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }
}
