package io.smartcat.cassandra.diagnostics.module.metrics;

import java.io.IOException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * Metrics collector class. Handles mbeans, jmx connection and collecting metrics.
 */
public class MetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);

    private static final String JXM_URL_FORMAT = "service:jmx:rmi:///jndi/rmi://[%s]:%d/jmxrmi";

    private static final String DEFAULT_SOCKET_FACTORY = "com.sun.jndi.rmi.factory.socket";

    private final MetricsConfiguration config;

    private JMXConnector jmxc;

    private MBeanServerConnection mbeanServerConn;

    private Set<MetricsMBean> mbeans;

    /**
     * Constructor.
     *
     * @param config metrics configuration
     */
    public MetricsCollector(final MetricsConfiguration config) {
        this.config = config;
    }

    /**
     * Close jmx connection.
     */
    public void close() {
        if (jmxc != null) {
            try {
                jmxc.close();
            } catch (IOException e) {
                logger.error("Error while trying to close jmx connection", e);
            }
            jmxc = null;
        }
    }

    /**
     * Open jmx connection and collect mbeans matching configuration defined patterns.
     *
     * @return success of connect and collect
     */
    public boolean connect() {
        try {
            JMXServiceURL jmxUrl = new JMXServiceURL(String.format(JXM_URL_FORMAT, config.jmxHost(), config.jmxPort()));
            Map<String, Object> env = new HashMap<String, Object>();
            if (config.jmxSslEnabled()) {
                String[] creds = new String[2];
                creds[0] = config.jmxSslUsername();
                creds[1] = config.jmxSslPassword();
                env.put(JMXConnector.CREDENTIALS, creds);
            }

            env.put(DEFAULT_SOCKET_FACTORY, getRMIClientSocketFactory());

            jmxc = JMXConnectorFactory.connect(jmxUrl, env);
            mbeanServerConn = jmxc.getMBeanServerConnection();

            String queryName = String.format("%s:*", config.metricsPackageName());
            mbeans = filterMBeans(mbeanServerConn.queryMBeans(new ObjectName(queryName), null));

            return true;
        } catch (IOException e) {
            logger.error("Cannot connect to jmx on {}:{}", config.jmxHost(), config.jmxPort(), e);
        } catch (MalformedObjectNameException e) {
            logger.error("Failed to query by object name", e);
        } catch (ReflectionException | IntrospectionException | InstanceNotFoundException e) {
            logger.error("Failed to get mbean attributes", e);
        }

        return false;
    }

    /**
     * Collect all measurement using defined mbeans.
     *
     * @return list of measurements
     */
    public List<Measurement> collectMeasurements() {
        List<Measurement> measurements = new ArrayList<Measurement>();

        for (final MetricsMBean mbean : mbeans) {
            for (final MBeanAttributeInfo attribute : mbean.getMBeanAttributes()) {
                try {
                    final Object value = mbeanServerConn
                            .getAttribute(mbean.getMBean().getObjectName(), attribute.getName());

                    if (value != null && !attribute.getType().equals(String.class.getName())) {
                        measurements.add(createMeasurement(mbean.getName() + "." + attribute.getName(),
                                Double.parseDouble(value.toString())));
                    }

                } catch (Exception e) {
                    logger.info("Exception while reading attributes", e);
                }
            }
        }

        return measurements;
    }

    private Measurement createMeasurement(String service, double rate) {
        return Measurement
                .create(service, rate, new Date().getTime(), TimeUnit.MILLISECONDS, new HashMap<String, String>(),
                        new HashMap<String, String>());
    }

    private Set<MetricsMBean> filterMBeans(final Set<ObjectInstance> mbeanObjectInstances)
            throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        Set<MetricsMBean> results = new HashSet<MetricsMBean>();
        List<Pattern> patterns = new ArrayList<Pattern>();
        for (String pattern : config.metricsPatterns()) {
            patterns.add(Pattern.compile(pattern));
        }

        for (ObjectInstance objectInstance : mbeanObjectInstances) {
            MetricsMBean mbean = new MetricsMBean(config.metricsPackageName(), objectInstance,
                    mbeanServerConn.getMBeanInfo(objectInstance.getObjectName()).getAttributes());

            boolean matches = false;
            if (patterns.isEmpty()) {
                matches = true;
            }

            for (Pattern pattern : patterns) {
                if (pattern.matcher(mbean.getName()).matches()) {
                    matches = true;
                }
            }

            if (matches) {
                results.add(mbean);
            }
        }

        for (MetricsMBean mbean : results) {
            logger.debug(mbean.toString());
        }

        return results;
    }

    private RMIClientSocketFactory getRMIClientSocketFactory() {
        if (config.jmxSslEnabled())
            return new SslRMIClientSocketFactory();
        else
            return RMISocketFactory.getDefaultSocketFactory();
    }

}
