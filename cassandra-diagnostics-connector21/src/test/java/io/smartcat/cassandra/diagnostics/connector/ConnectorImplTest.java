package io.smartcat.cassandra.diagnostics.connector;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.modules.agent.PowerMockAgent;

import io.smartcat.cassandra.diagnostics.Query;

public class ConnectorImplTest {
    public static QueryReporter rpt;

    @BeforeClass
    public static void setUp() throws ConfigurationException, TTransportException, IOException, InterruptedException {
        Instrumentation inst = PowerMockAgent.instrumentation();
        Connector connector = new ConnectorImpl();


        connector.init(inst, new QueryReporter() {
            @Override
            public void report(Query query) {
                System.out.println("Query report: " + query.toString());
            }
        });

        //EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }

}
