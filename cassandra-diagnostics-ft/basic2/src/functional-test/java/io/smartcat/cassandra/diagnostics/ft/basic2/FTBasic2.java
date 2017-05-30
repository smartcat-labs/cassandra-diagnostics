package io.smartcat.cassandra.diagnostics.ft.basic2;

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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import io.netty.util.internal.SystemPropertyUtil;

public class FTBasic2 {

    private static final String SELECT_QUERY = "SELECT * FROM test_keyspace.test_table";
    private static final String CASSANDRA_LOG = "system.log";

    private static Cluster cluster;
    private static Session session;
    private static WatchService watcher;
    private static FileSystem fileSystem;
    private static Path logFilePath;

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException, InterruptedException {
        cluster = Cluster.builder()
                .addContactPoint(SystemPropertyUtil.get("cassandra.host"))
                .withPort(Integer.parseInt(SystemPropertyUtil.get("cassandra.port")))
                .build();
        session = cluster.connect();
        session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace "
                + "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.test_table (uid uuid PRIMARY KEY);");

        fileSystem = FileSystems.getDefault();
        watcher = fileSystem.newWatchService();
        Path logFileDir = fileSystem.getPath(SystemPropertyUtil.get("project.build.directory"));
        logFilePath = fileSystem.getPath(SystemPropertyUtil.get("project.build.directory"), CASSANDRA_LOG);
        logFileDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    @AfterClass
    public static void cleanUp() {
        cluster.close();

        // this is needed in order to cleanup Cassandra log file since it is mounted from build directory which means
        // that on multiple runs test will show success from previous runs.
        logFilePath.toFile().delete();
    }

    @Test
    public void test() throws Exception {
        session.execute(SELECT_QUERY);

        verifyLogFileIsChanged();

        BufferedReader reader = new BufferedReader(new FileReader(logFilePath.toFile()));
        String line;
        boolean queryReportFound = false;
        boolean queryCountFound = false;
        boolean heartbeatFound = false;
        boolean requestRateFound = false;
        boolean repairSessionsFound = false;
        boolean compactionSettingsInfoFound = false;
        boolean numberOfUnreachableNodesFound = false;
        boolean nodeInfoFound = false;
        boolean hiccupFound = false;
        while ((line = reader.readLine()) != null) {
            if (line.matches(".* QUERYREPORT_COUNT.*")) {
                queryCountFound = true;
                continue;
            }
            if (line.matches(".* QUERYREPORT.*")) {
                queryReportFound = true;
                continue;
            }
            if (line.matches(".* HEARTBEAT.*")) {
                heartbeatFound = true;
                continue;
            }
            if (line.matches(".* REQUEST_RATE.*")) {
                requestRateFound = true;
                continue;
            }
            if (line.matches(".* REPAIR_SESSIONS.*")) {
                repairSessionsFound = true;
                continue;
            }
            if (line.matches(".* COMPACTION_SETTINGS_INFO.*")) {
                compactionSettingsInfoFound = true;
                continue;
            }
            if (line.matches(".* NUMBER_OF_UNREACHABLE_NODES.*")) {
                numberOfUnreachableNodesFound = true;
                continue;
            }
            if (line.matches(".* NODE_INFO.*")) {
                nodeInfoFound = true;
                continue;
            }
            if (line.matches(".* HICCUP.*")) {
                hiccupFound = true;
                continue;
            }
        }
        reader.close();

        Assertions.assertThat(queryReportFound).isTrue();
        Assertions.assertThat(queryCountFound).isTrue();
        Assertions.assertThat(heartbeatFound).isTrue();
        Assertions.assertThat(requestRateFound).isTrue();
        Assertions.assertThat(repairSessionsFound).isTrue();
        Assertions.assertThat(compactionSettingsInfoFound).isTrue();
        Assertions.assertThat(numberOfUnreachableNodesFound).isTrue();
        Assertions.assertThat(nodeInfoFound).isTrue();
        Assertions.assertThat(hiccupFound).isTrue();
    }

    public void verifyLogFileIsChanged() throws Exception {
        boolean logFileChanged = false;
        WatchKey watckKey = watcher.poll(10000, TimeUnit.MILLISECONDS);
        List<WatchEvent<?>> events = watckKey.pollEvents();
        for (WatchEvent<?> event : events) {
            final Path changed = (Path) event.context();
            if (changed.endsWith(CASSANDRA_LOG)) {
                logFileChanged = true;
            }
        }

        Assertions.assertThat(logFileChanged).isTrue();
    }
}
