package io.smartcat.cassandra.diagnostics.connector;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.instrument.InstrumentationSavingAgent;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.utils.EmbeddedCassandraServerHelper;

public class ConnectorImplTest {

    private static Session session;
    private static boolean queryIntercepted;

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException, InterruptedException {
        queryIntercepted = false;
        Instrumentation inst = InstrumentationSavingAgent.getInstrumentation();
        Connector connector = new ConnectorImpl();
        connector.init(inst, new QueryReporter() {
            @Override
            public void report(Query query) {
                if (Query.StatementType.SELECT.equals(query.statementType()) &&
                        "test_keyspace".equalsIgnoreCase(query.keyspace()) &&
                        "test_table".equalsIgnoreCase(query.tableName())) {
                    queryIntercepted = true;
                }
            }
        });
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        session = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .withPort(9142)
                .build()
                .connect();
    }

    @Test
    public void test() {
        session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace " +
                "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.test_table (uid uuid PRIMARY KEY);");
        session.execute("SELECT * FROM test_keyspace.test_table");
        sleep(1000);
        session.close();
        Assert.assertTrue(queryIntercepted);
    }

    private static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
