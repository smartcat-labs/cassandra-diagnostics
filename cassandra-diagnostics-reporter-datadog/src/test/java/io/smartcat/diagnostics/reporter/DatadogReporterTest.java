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
        final TestUDPServer testUDPServer = new TestUDPServer(1);
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

        final Measurement measurement = Measurement
                .create("test-metric", 909.0, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);

        reporter.report(measurement);

        runner.join(500);

        assertThat(testUDPServer.receivedMessages.size()).isEqualTo(1);
        assertThat(testUDPServer.receivedMessages.get(0))
                .contains("test-metric:909|g|#host:somehost,tag2:two,tag3:three,tag2:tv2,tag1:tv1");

        testUDPServer.stop();
    }

    @Test
    public void should_send_measurement_for_each_field_in_complex_measurement()
            throws SocketException, InterruptedException, UnknownHostException {
        final TestUDPServer testUDPServer = new TestUDPServer(4);
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
        fields.put("v1", "15.0");
        fields.put("v2", "26.0");
        fields.put("v3", "50.0");
        fields.put("v4", "1234.0");

        final Measurement measurement = Measurement
                .create("test-metric", null, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);

        reporter.report(measurement);
        runner.join(500);

        assertThat(testUDPServer.receivedMessages.size()).isEqualTo(4);

        assertThat(testUDPServer.receivedMessages.get(0))
                .isEqualTo("prefix.test-metric.v1:15|g|#host:somehost,tag2:two,tag3:three,tag2:tv2,tag1:tv1");
        assertThat(testUDPServer.receivedMessages.get(1))
                .isEqualTo("prefix.test-metric.v2:26|g|#host:somehost,tag2:two,tag3:three,tag2:tv2,tag1:tv1");
        assertThat(testUDPServer.receivedMessages.get(2))
                .isEqualTo("prefix.test-metric.v3:50|g|#host:somehost,tag2:two,tag3:three,tag2:tv2,tag1:tv1");
        assertThat(testUDPServer.receivedMessages.get(3))
                .isEqualTo("prefix.test-metric.v4:1234|g|#host:somehost,tag2:two,tag3:three,tag2:tv2,tag1:tv1");

        testUDPServer.stop();
    }

    @Test
    public void should_skip_non_number_fields_in_complex_measurement()
            throws SocketException, InterruptedException, UnknownHostException {
        final TestUDPServer testUDPServer = new TestUDPServer(4);
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
        fields.put("v1", "1a");
        fields.put("v2", "df");
        fields.put("v3", ".");
        fields.put("v4", "1234.0");
        fields.put("v5", "-");

        final Measurement measurement = Measurement
                .create("test-metric", null, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);

        reporter.report(measurement);
        runner.join(500);

        assertThat(testUDPServer.receivedMessages.size()).isEqualTo(1);
        assertThat(testUDPServer.receivedMessages.get(0))
                .isEqualTo("prefix.test-metric.v4:1234|g|#host:somehost,tag2:two,tag3:three,tag2:tv2,tag1:tv1");

        testUDPServer.stop();
    }

    private class TestUDPServer implements Runnable {

        private DatagramSocket serverSocket = new DatagramSocket(9876);

        private byte[] receiveData = new byte[1024];

        private DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        private final int expectedMessages;

        private TestUDPServer(int expectedMessages) throws SocketException, UnknownHostException {
            this.expectedMessages = expectedMessages;
        }

        public List<String> receivedMessages = new ArrayList<>();

        public void run() {
            try {
                while (true) {
                    serverSocket.receive(receivePacket);
                    String[] messages = new String(receivePacket.getData()).split("\n");
                    for (String message : messages) {
                        receivedMessages.add(message.trim());
                    }
                    if (receivedMessages.size() == this.expectedMessages) {
                        break;
                    }
                }
            } catch (IOException e) {
                assert false;
            }
        }

        public void stop() {
            serverSocket.close();
            serverSocket = null;
        }
    }

}
