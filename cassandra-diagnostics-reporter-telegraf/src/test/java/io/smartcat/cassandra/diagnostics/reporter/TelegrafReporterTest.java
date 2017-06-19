package io.smartcat.cassandra.diagnostics.reporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.smartcat.cassandra.diagnostics.config.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.measurement.Measurement;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TelegrafReporter.class })
public class TelegrafReporterTest {

    private String line = "dummy";

    @Test
    public void measurement_send() throws Exception {

        TcpClient tcpClientMock = mock(TcpClient.class);
        when(tcpClientMock.isConnected()).thenReturn(true);
        doNothing().when(tcpClientMock).start();
        doNothing().when(tcpClientMock).stop();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ByteBuffer buf = (ByteBuffer) invocation.getArguments()[0];
                Charset charset = StandardCharsets.UTF_8;
                CharsetDecoder decoder = charset.newDecoder();
                line = decoder.decode(buf).toString();
                return null;
            }
        }).when(tcpClientMock).send(any(ByteBuffer.class));

        ReporterConfiguration configuration = new ReporterConfiguration();
        configuration.options.put("telegrafHost", "localhost");

        TelegrafReporter reporter = new TelegrafReporter(configuration, GlobalConfiguration.getDefault());
        MemberModifier.field(TelegrafReporter.class, "telegrafClient").set(reporter, tcpClientMock);

        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new HashMap<>();
        fields.put("v2", "abc");
        final Measurement measurement = Measurement.createSimple("m1", 1.0, 1434055662L, tags, fields);

        reporter.report(measurement);
        assertThat(line).isEqualTo("m1,tag1=tv1,tag2=tv2,type=SIMPLE v2=\"abc\",value=1.0 1434055662000000\r\n");

    }
}