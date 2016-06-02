package io.smartcat.cassandra.diagnostics.connector;

import static org.junit.Assert.fail;

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
    private static int queryCount;

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException, InterruptedException {
        queryCount = 0;
        Instrumentation inst = InstrumentationSavingAgent.getInstrumentation();
        Connector connector = new ConnectorImpl();
        connector.init(inst, new QueryReporter() {
            @Override
            public void report(Query query) {
                queryCount++;
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
        session.execute("SELECT * FROM system.local");
        sleep(1000);
        session.close();
        Assert.assertTrue(queryCount > 0);
    }

    private static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
