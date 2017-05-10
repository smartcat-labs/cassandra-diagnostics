package io.smartcat.cassandra.diagnostics.ft.riemann;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.assertj.core.api.Assertions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import io.netty.util.internal.SystemPropertyUtil;

public class FTRiemann {

    private static final String INFLUXDB_NAME = "diagnostics-test";

    private static Cluster cluster;
    private static Session session;
    private static InfluxDB influxdb;

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException, InterruptedException {
        cluster = Cluster.builder()
                .addContactPoint(SystemPropertyUtil.get("cassandra.host"))
                .withPort(Integer.parseInt(SystemPropertyUtil.get("cassandra.port")))
                .build();
        session = cluster.connect();

        influxdb = InfluxDBFactory.connect(SystemPropertyUtil.get("influxdb.url"), 
                SystemPropertyUtil.get("influxdb.user"), SystemPropertyUtil.get("influxdb.password"));
        influxdb.createDatabase(INFLUXDB_NAME);
    }

    @Test
    public void test() throws Exception {
        FileSystem fileSystem = FileSystems.getDefault();
        WatchService watcher = fileSystem.newWatchService();
        Path logFileDir = fileSystem.getPath(SystemPropertyUtil.get("project.build.directory"));
        logFileDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

        session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace "
                + "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.test_table (uid uuid PRIMARY KEY);");

        session.execute("SELECT * FROM test_keyspace.test_table");

        QueryResult result = null;
        for (int i = 0; i < 10; i++) {
            result = influxdb.query(new Query("SHOW SERIES FROM \"queryReport\"", INFLUXDB_NAME));
            if (!result.hasError()) {
                break;
            }
            Thread.sleep(500);
        }

        Assertions.assertThat(result.getResults().size()).isEqualTo(1);
        cluster.close();
    }
}
