package io.smartcat.cassandra.diagnostics.report;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aphyr.riemann.client.IRiemannClient;
import com.aphyr.riemann.client.RiemannBatchClient;
import com.aphyr.riemann.client.RiemannClient;
import com.aphyr.riemann.client.UnsupportedJVMException;
import com.google.inject.Inject;

import io.smartcat.cassandra.diagnostics.config.Configuration;

/**
 * A Riemann based {@link QueryReporter} implementation. Query reports
 * are sending towards the configured Riemann server as Riemann events.
 */
public class RiemannQueryReporter implements QueryReporter {

  private static final String HOST_PROP = "riemannHost";

  private static final String DEFAULT_PORT_PROP = "riemannPort";

  private static final String DEFAULT_PORT = "5555";

  private static final String DEFAULT_BATCH_SIZE_PROP = "riemannBatchSize";

  private static final String DEFAULT_BATCH_SIZE = "10";

  private static final String EVENT_SERVICE_NAME = "queryReport";

  /**
   * Class logger.
   */
  private static final Logger logger = LoggerFactory.getLogger(RiemannQueryReporter.class);

  private Configuration config;

  private static IRiemannClient riemann;

  /**
   * Constructor.
   *
   * @param config configuration
   */
  @Inject
  public RiemannQueryReporter(Configuration config) {
    this.config = config;
  }

  @Override
  public void report(QueryReport queryReport) {
    if (riemannClient() != null) {
      logger.debug("Sending QueryReport: execTime=" + queryReport.executionTime);
      riemannClient().event()
        .service(EVENT_SERVICE_NAME)
        .state("ok")
        //.time(queryReport.startTime / 1000000)
        .metric(queryReport.executionTime)
        .attribute("client", queryReport.clientAddress)
        .attribute("statement", queryReport.statement)
        .send();
    }
  }

  private IRiemannClient riemannClient() {
    if (riemann == null) {
      initRiemannClient(config);
    }
    return riemann;
  }

  private static synchronized void initRiemannClient(Configuration config) {
    if (riemann != null) return;
    if (config.reporterOptions.containsKey(HOST_PROP)) {
      String host = config.reporterOptions.get(HOST_PROP);
      int port = Integer.parseInt(config.reporterOptions.getOrDefault(DEFAULT_PORT_PROP, DEFAULT_PORT));
      int batchSize = Integer.parseInt(
          config.reporterOptions.getOrDefault(DEFAULT_BATCH_SIZE_PROP, DEFAULT_BATCH_SIZE));
      try {
        riemann = new RiemannBatchClient(RiemannClient.tcp(host, port), batchSize);
        riemann.connect();
      } catch (IOException | UnsupportedJVMException e) {
        logger.warn("Riemann client cannot be initialized", e);
      }
    } else {
      logger.warn("Tried to init Riemann client. Not properly configured. Aborting initialization.");
    }
  }

}
