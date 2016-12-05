package io.smartcat.cassandra.diagnostics.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * Implements diagnostics HTTP API.
 */
public class HttpHandler extends NanoHTTPD {
    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    private DiagnosticsApi mxBean;

    private final boolean apiAuthEnabled;
    private final String apiKey;

    /**
     * Constructor.
     *
     * @param config diagnostics configuration
     * @param mxBean diagnostics control bean
     */
    public HttpHandler(Configuration config, DiagnosticsApi mxBean) {
        super(config.httpApiHost, config.httpApiPort);
        this.mxBean = mxBean;

        apiAuthEnabled = config.httpApiAuthEnabled;
        apiKey = config.httpApiKey;
    }

    /* (non-Javadoc)
     * @see fi.iki.elonen.NanoHTTPD#serve(fi.iki.elonen.NanoHTTPD.IHTTPSession)
     */
    @Override
    public Response serve(IHTTPSession session) {
        if (!apiAuthEnabled || hasValidCredentials(session)) {
            return respond(session);
        }

        return newFixedLengthResponse(Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Invalid API key");
    }

    private Response respond(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();

        logger.debug("Serving {} {} request.", method, uri);
        if (Method.GET.equals(method) && "/version".equalsIgnoreCase(uri)) {
            return newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, mxBean.getVersion());
        } else if (Method.POST.equals(method) && "/reload".equalsIgnoreCase(uri)) {
            mxBean.reload();
            return newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT,
                    "Configuration reloaded");
        } else {
            return newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
                    "Requested URI " + uri + " not found");
        }
    }

    private boolean hasValidCredentials(IHTTPSession session) {
        String header = session.getHeaders().get("authorization");
        return apiKey.equals(header);
    }
}
