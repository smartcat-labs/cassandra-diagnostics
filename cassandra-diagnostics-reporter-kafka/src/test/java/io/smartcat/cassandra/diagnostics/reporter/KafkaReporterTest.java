package io.smartcat.cassandra.diagnostics.reporter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import io.smartcat.cassandra.diagnostics.Measurement;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.zk.EmbeddedZookeeper;

public class KafkaReporterTest {
    private static final String HOST = "127.0.0.1";
    private static final String BROKER_PORT = "9092";

    private EmbeddedZookeeper zkServer;
    private ZkClient zkClient;
    private KafkaServer kafkaServer;

    @Before
    public void initialise() throws InterruptedException, IOException {
        zkServer = new EmbeddedZookeeper();

        String zkConnect = HOST + ":" + zkServer.port();
        int timeout = 30000;
        zkClient = new ZkClient(zkConnect, timeout, timeout, ZKStringSerializer$.MODULE$);

        Properties brokerProps = new Properties();
        brokerProps.put("zookeeper.connect", zkConnect);
        brokerProps.put("broker.id", "0");
        brokerProps.put("log.dirs", Files.createTempDirectory("kafka-").toAbsolutePath().toString());
        brokerProps.put("listeners", "PLAINTEXT://" + HOST + ":" + BROKER_PORT);

        KafkaConfig config = new KafkaConfig(brokerProps);
        kafkaServer = TestUtils.createServer(config, new MockTime());
    }

    @Test
    public void send_measurements() {
        ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("kafkaBootstrapServers", HOST + ":" + BROKER_PORT);
        KafkaReporter reporter = new KafkaReporter(config);

        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");

        Measurement measurement = Measurement.create("m1", 1.0, 1434055662, TimeUnit.SECONDS, tags, fields);
        reporter.report(measurement);

        // setup consumer
        Properties consumerProps = new Properties();
        consumerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, HOST + ":" + BROKER_PORT);
        consumerProps.put("group.id", "test-group");
        consumerProps.put("client.id", "test-consumer");
        consumerProps.put("auto.offset.reset", "earliest");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(measurement.name()));

        ConsumerRecords<String, String> records = consumer.poll(30000);
        assertEquals(1, records.count());

        Iterator<ConsumerRecord<String, String>> recordIterator = records.iterator();
        ConsumerRecord<String, String> record = recordIterator.next();

        assertEquals(Whitebox.getInternalState(reporter, "partitionKey"), record.key());
        assertEquals(measurement.toJson(), record.value());
    }

    @After
    public void destroy() {
        kafkaServer.shutdown();
        zkClient.close();
        zkServer.shutdown();
    }
}
