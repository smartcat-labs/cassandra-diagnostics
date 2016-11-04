package io.smartcat.cassandra.diagnostics.reporter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * Apache Kafka based {@Link Reporter} implementation. All measurements are written into Kafka based on measurements
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
        partitionKey = hostname();
    }

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Cannot resolve local host partitionKey");
            return "HOST_UNKNOWN";
        }
    }

    @Override
    public void report(Measurement measurement) {
        if (producer == null) {
            logger.warn("Kafka producer is not initialized.");
            return;
        }

        ProducerRecord<String, String> record =
                new ProducerRecord<>(measurement.name(), partitionKey, measurement.toString());

        producer.send(record);
    }

    @Override
    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }
}
