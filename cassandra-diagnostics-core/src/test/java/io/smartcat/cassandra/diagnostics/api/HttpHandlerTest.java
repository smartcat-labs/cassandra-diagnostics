package io.smartcat.cassandra.diagnostics.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.Test;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import io.smartcat.cassandra.diagnostics.config.Configuration;

public class HttpHandlerTest {

    @Test
    public void not_found() {
        Configuration config = Configuration.getDefaultConfiguration();
        DiagnosticsMXBean mxBean = mock(DiagnosticsMXBean.class);
        HttpHandler httpApi = new HttpHandler(config, mxBean);
        IHTTPSession session = mock(IHTTPSession.class);
        when(session.getMethod()).thenReturn(Method.GET);
        when(session.getUri()).thenReturn("/someuri");
        Response res = httpApi.serve(session);
        assertThat(res.getStatus()).isEqualTo(Status.NOT_FOUND);
    }

    @Test
    public void get_version() {
        Configuration config = Configuration.getDefaultConfiguration();
        DiagnosticsMXBean mxBean = mock(DiagnosticsMXBean.class);
        when(mxBean.getVersion()).thenReturn("1.2.3");
        HttpHandler httpApi = new HttpHandler(config, mxBean);
        IHTTPSession session = mock(IHTTPSession.class);
        when(session.getMethod()).thenReturn(Method.GET);
        when(session.getUri()).thenReturn("/version");
        Response res = httpApi.serve(session);
        assertThat(res.getStatus()).isEqualTo(Status.OK);
        String text = new BufferedReader(new InputStreamReader(res.getData())).lines().collect(Collectors.joining());
        assertThat(text).isEqualTo("1.2.3");
    }

    @Test
    public void reload() {
        Configuration config = Configuration.getDefaultConfiguration();
        DiagnosticsMXBean mxBean = mock(DiagnosticsMXBean.class);
        HttpHandler httpApi = new HttpHandler(config, mxBean);
        IHTTPSession session = mock(IHTTPSession.class);
        when(session.getMethod()).thenReturn(Method.POST);
        when(session.getUri()).thenReturn("/reload");
        Response res = httpApi.serve(session);
        assertThat(res.getStatus()).isEqualTo(Status.OK);
        verify(mxBean).reload();
    }

}
