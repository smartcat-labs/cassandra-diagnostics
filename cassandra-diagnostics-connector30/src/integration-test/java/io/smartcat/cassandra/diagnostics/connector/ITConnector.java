package io.smartcat.cassandra.diagnostics.connector;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.instrument.InstrumentationSavingAgent;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Query;
import io.smartcat.cassandra.diagnostics.utils.EmbeddedCassandraServerHelper;

public class ITConnector {

    private static Cluster cluster;
    private static Session session;
    private static CountDownLatch lockForPreparedStatement = new CountDownLatch(1);
    private static boolean preparedQueryIntercepted;
    private static CountDownLatch lockForUnpreparedStatement = new CountDownLatch(1);
    private static boolean unpreparedQueryIntercepted;

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException, InterruptedException {
        preparedQueryIntercepted = false;
        unpreparedQueryIntercepted = false;

        final Instrumentation inst = InstrumentationSavingAgent.getInstrumentation();
        ConnectorConfiguration configuration = new ConnectorConfiguration();
        configuration.enableTracing = true;
        final Connector connector = new ConnectorImpl();
        GlobalConfiguration globalConfiguration = GlobalConfiguration.getDefault();
        connector.init(inst, new QueryReporter() {
            @Override
            public void report(Query query) {
                if (Query.StatementType.SELECT.equals(query.statementType()) && "test_keyspace"
                        .equalsIgnoreCase(query.keyspace()) && "test_table_prepared".equalsIgnoreCase(query.tableName()) &&
                        query.statement().equalsIgnoreCase("SELECT uid FROM test_keyspace.test_table_prepared")) {
                    preparedQueryIntercepted = true;
                    lockForPreparedStatement.countDown();
                }
                if (Query.StatementType.SELECT.equals(query.statementType()) && "test_keyspace"
                        .equalsIgnoreCase(query.keyspace()) && "test_table_unprepared".equalsIgnoreCase(query.tableName()) &&
                        query.statement().equalsIgnoreCase("SELECT uid FROM test_keyspace.test_table_unprepared")) {
                    unpreparedQueryIntercepted = true;
                    lockForUnpreparedStatement.countDown();
                }
            }
        }, configuration, globalConfiguration);
        
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        connector.waitForSetupCompleted();
        Thread.sleep(4000);
        cluster = Cluster.builder().addContactPoint("127.0.0.1").withPort(9142).build();
        session = cluster.connect();
    }

    @AfterClass
    public static void cleanUp() {
        cluster.close();
    }

    @Test
    public void prepared_query_is_intercepted_when_connector_is_active() throws InterruptedException {
        session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace "
                + "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.test_table_prepared (uid uuid PRIMARY KEY);");
        String cql = "SELECT uid FROM test_keyspace.test_table_prepared";
        PreparedStatement prepared = session.prepare(cql);
        session.execute(prepared.bind());
        lockForPreparedStatement.await(60000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(preparedQueryIntercepted);
    }

    @Test
    public void unprepared_query_is_intercepted_when_connector_is_active() throws InterruptedException {
        session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace "
                + "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.test_table_unprepared (uid uuid PRIMARY KEY);");
        String cql = "SELECT uid FROM test_keyspace.test_table_unprepared";
        session.execute(cql);
        lockForUnpreparedStatement.await(60000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(unpreparedQueryIntercepted);
    }    
}
