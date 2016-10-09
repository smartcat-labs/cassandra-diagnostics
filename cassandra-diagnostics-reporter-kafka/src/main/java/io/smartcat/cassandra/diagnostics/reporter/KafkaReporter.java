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

    private final String hostname;

    private final Producer<String, Measurement> kafkaProducer;

    /**
     * Constructor.
     *
     * @param configuration Reporter configuration
     */
    public KafkaReporter(ReporterConfiguration configuration) {
        super(configuration);

        hostname = getHostname();

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.kafkaProducer = new KafkaProducer<String, Measurement>(properties);
    }

    @Override
    public void report(Measurement measurement) {
        ProducerRecord<String, Measurement> record = new ProducerRecord<String, Measurement>(measurement.name(),
                hostname, measurement);
        kafkaProducer.send(record);
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Cannot resolve local host hostname");
            return "HOST_UNKNOWN";
        }
    }
}
