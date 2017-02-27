package io.smartcat.cassandra.diagnostics.connector;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of diagnostics event processor. It implements event queuing,
 * asynchronous execution and throttling.
 */
public abstract class AbstractEventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEventProcessor.class);

    private static final AtomicLong THREAD_COUNT = new AtomicLong(0);

    /**
     * Executor service used for executing query reports.
     */
    private ThreadPoolExecutor executor;

    /**
     * Query reporter.
     */
    protected QueryReporter queryReporter;

    /**
     * Connector implementation specific configuration.
     */
    protected ConnectorConfiguration configuration;

    private static boolean queueOverflow = false;

    /**
     * Constructor.
     *
     * @param queryReporter QueryReporter used to report queries
     * @param configuration Connector configuration
     */
    public AbstractEventProcessor(QueryReporter queryReporter, ConnectorConfiguration configuration) {
        this.queryReporter = queryReporter;
        this.configuration = configuration;
        executor = new ThreadPoolExecutor(configuration.numWorkerThreads,
                configuration.numWorkerThreads,
                100L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = new Thread(runnable);
                        thread.setName("cassandra-diagnostics-connector-" + THREAD_COUNT.getAndIncrement());
                        thread.setDaemon(true);
                        thread.setPriority(Thread.MIN_PRIORITY);
                        return thread;
                    }
                });
    }

    /**
     * Submits a query reports asynchronously.
     *
     * @param reportAction action to be executed
     */
    protected void report(final Runnable reportAction) {
        int numQueuedEvents = executor.getQueue().size();

        if (!queueOverflow) {
            executor.submit(reportAction);
            if (numQueuedEvents > configuration.queuedEventsOverflowThreshold) {
                queueOverflow = true;
                logger.warn("Event queue overflown. Until relaxed, further events will be dropped.");
            }
        } else {
            if (numQueuedEvents <= configuration.queuedEventsRelaxThreshold) {
                queueOverflow = false;
                logger.info("Event queue relaxed. Further events will be accepted and processed.");
            } else {
                logger.trace("Event queue overflown. Event is dropped.");
            }
        }
    }

}
