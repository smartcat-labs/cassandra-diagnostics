package io.smartcat.cassandra.diagnostics.reporter;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;

public class KafkaLocal {

    private static final Logger logger = LoggerFactory.getLogger(KafkaLocal.class);

    public final KafkaServerStartable kafka;
    public final ZooKeeperLocal zookeeper;

    public KafkaLocal(Properties kafkaProperties, Properties zkProperties) throws IOException, InterruptedException {
        KafkaConfig kafkaConfig = new KafkaConfig(kafkaProperties);

        //start local zookeeper
        logger.info("starting local zookeeper...");
        zookeeper = new ZooKeeperLocal(zkProperties);
        logger.info("done");

        //start local kafka broker
        kafka = new KafkaServerStartable(kafkaConfig);
        logger.info("starting local kafka broker...");
        kafka.startup();
        logger.info("done");
    }

    public void stop() {
        //stop kafka broker
        logger.info("stopping kafka...");
        kafka.shutdown();
        logger.info("done");
    }

}
