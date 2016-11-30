package io.smartcat.cassandra.diagnostics.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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
        DiagnosticsApi mxBean = mock(DiagnosticsApi.class);
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
        DiagnosticsApi mxBean = mock(DiagnosticsApi.class);
        when(mxBean.getVersion()).thenReturn("1.2.3");
        HttpHandler httpApi = new HttpHandler(config, mxBean);
        IHTTPSession session = mock(IHTTPSession.class);
        when(session.getMethod()).thenReturn(Method.GET);
        when(session.getUri()).thenReturn("/version");
        Response res = httpApi.serve(session);
        assertThat(res.getStatus()).isEqualTo(Status.OK);

        try {
            String text = new BufferedReader(new InputStreamReader(res.getData())).readLine();
            assertThat(text).isEqualTo("1.2.3");
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void reload() {
        Configuration config = Configuration.getDefaultConfiguration();
        DiagnosticsApi mxBean = mock(DiagnosticsApi.class);
        HttpHandler httpApi = new HttpHandler(config, mxBean);
        IHTTPSession session = mock(IHTTPSession.class);
        when(session.getMethod()).thenReturn(Method.POST);
        when(session.getUri()).thenReturn("/reload");
        Response res = httpApi.serve(session);
        assertThat(res.getStatus()).isEqualTo(Status.OK);
        verify(mxBean).reload();
    }

    @Test
    public void get_version_valid_api_key() {
        Configuration config = Configuration.getDefaultConfiguration();
        config.httpApiAuthEnabled = true;

        DiagnosticsApi mxBean = mock(DiagnosticsApi.class);
        when(mxBean.getVersion()).thenReturn("1.2.3");

        HttpHandler httpApi = new HttpHandler(config, mxBean);
        IHTTPSession session = mock(IHTTPSession.class);
        when(session.getMethod()).thenReturn(Method.GET);

        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", config.httpApiKey);
        when(session.getHeaders()).thenReturn(headers);
        when(session.getUri()).thenReturn("/version");

        Response res = httpApi.serve(session);
        assertThat(res.getStatus()).isEqualTo(Status.OK);

        try {
            String text = new BufferedReader(new InputStreamReader(res.getData())).readLine();
            assertThat(text).isEqualTo("1.2.3");
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void get_version_missing_api_key() {
        Configuration config = Configuration.getDefaultConfiguration();
        config.httpApiAuthEnabled = true;

        DiagnosticsApi mxBean = mock(DiagnosticsApi.class);
        HttpHandler httpApi = new HttpHandler(config, mxBean);
        IHTTPSession session = mock(IHTTPSession.class);
        when(session.getMethod()).thenReturn(Method.GET);
        when(session.getUri()).thenReturn("/version");

        Response res = httpApi.serve(session);
        assertThat(res.getStatus()).isEqualTo(Status.FORBIDDEN);
    }

    @Test
    public void get_version_invalid_api_key() {
        Configuration config = Configuration.getDefaultConfiguration();
        config.httpApiAuthEnabled = true;

        DiagnosticsApi mxBean = mock(DiagnosticsApi.class);
        HttpHandler httpApi = new HttpHandler(config, mxBean);
        IHTTPSession session = mock(IHTTPSession.class);
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "invalid-key");
        when(session.getHeaders()).thenReturn(headers);
        when(session.getUri()).thenReturn("/version");
        when(session.getMethod()).thenReturn(Method.GET);
        when(session.getUri()).thenReturn("/version");

        Response res = httpApi.serve(session);
        assertThat(res.getStatus()).isEqualTo(Status.FORBIDDEN);
    }
}
