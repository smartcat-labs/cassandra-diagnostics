package io.smartcat.diagnostics.reporter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.smartcat.cassandra.diagnostics.Measurement;
import io.smartcat.cassandra.diagnostics.reporter.DatadogReporter;
import io.smartcat.cassandra.diagnostics.reporter.ReporterConfiguration;

public class DatadogReporterTest {

    @Test
    public void should_load_configuration() {
        final ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("statsDHost", "localhost");
        config.options.put("statsDPort", 7000);
        config.options.put("keysPrefix", "");
        config.options.put("fixedTags", Arrays.asList("host:hostname"));

        final DatadogReporter reporter = new DatadogReporter(config);

        reporter.stop();
    }

    @Test(expected = ClassCastException.class)
    public void should_fail_to_load_configuration_port() {
        final ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("statsDPort", "NONE");

        final DatadogReporter reporter = new DatadogReporter(config);

        reporter.stop();

        assert false;
    }

    @Test(expected = ClassCastException.class)
    public void should_fail_to_load_configuration_tags() {
        final ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("fixedTags", new Integer(0));

        final DatadogReporter reporter = new DatadogReporter(config);

        reporter.stop();

        assert false;
    }

    @Test
    public void should_send_measurement() throws SocketException, InterruptedException, UnknownHostException {
        final TestUDPServer testUDPServer = new TestUDPServer();
        Thread runner = new Thread(testUDPServer);
        runner.start();

        final ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("statsDHost", "localhost");
        config.options.put("statsDPort", 9876);
        config.options.put("keysPrefix", "prefix");
        config.options.put("fixedTags", Arrays.asList("host:somehost,tag2:two,tag3:three"));
        final DatadogReporter reporter = new DatadogReporter(config);

        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");

        final Measurement measurement = Measurement.create("test-metric", 909, System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, fields);

        reporter.report(measurement);

        runner.join();

        assertThat(testUDPServer.receivedMessages.size()).isEqualTo(1);
        assertThat(testUDPServer.receivedMessages.get(0))
                .contains("test-metric:909|g|#host:somehost,tag2:two,tag3:three,tag2:tv2,tag1:tv1");
    }

    private class TestUDPServer implements Runnable {

        private DatagramSocket serverSocket = new DatagramSocket(9876);

        private byte[] receiveData = new byte[1024];

        private TestUDPServer() throws SocketException, UnknownHostException {

        }

        public List<String> receivedMessages = new ArrayList<>();

        public void run() {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String message = new String(receivePacket.getData());

                receivedMessages.add(message);
            } catch (IOException e) {
                assert false;
            }
        }
    }

}
