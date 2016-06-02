package io.smartcat.cassandra.diagnostics.connector;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.utils.EmbeddedCassandraServerHelper;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.instrument.InstrumentationSavingAgent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class ConnectorImplTest {

    private static Cluster cluster;
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
        cluster = Cluster.builder().addContactPoint("127.0.0.1").withPort(9142).build();
        session = cluster.connect();
    }

    @Test
    public void test() {
        session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace "
                + "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.test_table (uid uuid PRIMARY KEY);");
        session.execute("SELECT * FROM test_keyspace.test_table");
        cluster.close();
        Assert.assertTrue(queryIntercepted);
    }

}
