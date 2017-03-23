package io.smartcat.cassandra.diagnostics.connector;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.instrument.InstrumentationSavingAgent;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.utils.EmbeddedCassandraServerHelper;

public class ITConnector {

    private static Cluster cluster;
    private static Session session;
    private static CountDownLatch lock = new CountDownLatch(2);
    private static int queryInterceptedCnt;

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException, InterruptedException {
        queryInterceptedCnt = 0;

        final Instrumentation inst = InstrumentationSavingAgent.getInstrumentation();
        ConnectorConfiguration configuration = new ConnectorConfiguration();
        configuration.enableTracing = true;
        final Connector connector = new ConnectorImpl();
        connector.init(inst, new QueryReporter() {
            @Override
            public void report(Query query) {
                if (Query.StatementType.SELECT.equals(query.statementType()) && "test_keyspace"
                        .equalsIgnoreCase(query.keyspace()) && "test_table".equalsIgnoreCase(query.tableName()) &&
                        query.statement().equalsIgnoreCase("SELECT uid FROM test_keyspace.test_table")) {
                    queryInterceptedCnt++;
                    lock.countDown();
                }
            }
        }, configuration);
        
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        connector.waitForSetupCompleted();
        Thread.sleep(4000);
        cluster = Cluster.builder().addContactPoint("127.0.0.1").withPort(9142).build();
        session = cluster.connect();
    }

    @Test
    public void query_is_intercepted_when_connector_is_active() throws InterruptedException {
        session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace "
                + "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.test_table (uid uuid PRIMARY KEY);");
        String cql = "SELECT uid FROM test_keyspace.test_table";
        PreparedStatement prepared = session.prepare(cql);
        session.execute(cql);
        session.execute(prepared.bind().setReadTimeoutMillis(20000).setDefaultTimestamp(20000));
        Thread.sleep(2000);
        cluster.close();
        lock.await(60000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(2, queryInterceptedCnt);
    }

}
