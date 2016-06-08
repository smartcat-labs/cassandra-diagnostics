package io.smartcat.cassandra.diagnostics.ft.basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import io.netty.util.internal.SystemPropertyUtil;

public class FTBasic {

    private static Cluster cluster;
    private static Session session;

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException, InterruptedException {
        cluster = Cluster.builder()
                .addContactPoint(SystemPropertyUtil.get("cassandra.host"))
                .withPort(Integer.parseInt(SystemPropertyUtil.get("cassandra.port")))
                .build();
        session = cluster.connect();
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
        cluster.close();

        boolean logFileChanged = false;
        WatchKey watckKey = watcher.poll(10000, TimeUnit.MILLISECONDS);
        List<WatchEvent<?>> events = watckKey.pollEvents();
        for (WatchEvent<?> event : events) {
            final Path changed = (Path) event.context();
            if (changed.endsWith("system.log")) {
                logFileChanged = true;
            }
        }

        Assertions.assertThat(logFileChanged).isTrue();
        Path logFilePath = fileSystem.getPath(SystemPropertyUtil.get("project.build.directory"), "system.log");

        BufferedReader reader = new BufferedReader(new FileReader(logFilePath.toFile()));
        String line;
        boolean logFound = false;
        while ((line = reader.readLine()) != null) {
            if (line.matches(".* LogQueryReporter\\.java.*")) {
                logFound = true;
            }
        }
        reader.close();
        Assertions.assertThat(logFound).isTrue();
    }
}
