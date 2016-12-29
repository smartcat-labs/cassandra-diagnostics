package io.smartcat.cassandra.diagnostics.connector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import io.smartcat.cassandra.utils.EmbeddedCassandraServerHelper;

public class ITConnector {

    private static Cluster cluster;
    private static Session session;
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException, InterruptedException {
        System.setOut(new PrintStream(outContent));
        ConnectorConfiguration configuration = new ConnectorConfiguration();
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        cluster = Cluster.builder().addContactPoint("127.0.0.1").withPort(9142).build();
        session = cluster.connect();
    }

    @Test
    public void query_is_intercepted_when_connector_is_active() throws InterruptedException {
        session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace "
                + "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.test_table (uid uuid PRIMARY KEY);");
        session.execute("SELECT * FROM test_keyspace.test_table");

        Assertions.assertThat(outContent.toString())
        .contains("statement=SELECT * FROM test_keyspace.test_table, keyspace=test_keyspace, tableName=test_table");
    }

    @AfterClass
    public static void cleanUpStreams() {
        System.setOut(null);
        cluster.close();
    }

}
