package io.smartcat.cassandra.diagnostics.module.metrics;

import java.io.IOException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.ArrayList;
import java.util.Arrays;
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

import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;

/**
 * Metrics collector class. Handles mbeans, jmx connection and collecting metrics.
 */
public class MetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);

    private static final String JXM_URL_FORMAT = "service:jmx:rmi:///jndi/rmi://[%s]:%d/jmxrmi";

    private static final String DEFAULT_SOCKET_FACTORY = "com.sun.jndi.rmi.factory.socket";

    private final MetricsConfiguration config;

    private final GlobalConfiguration globalConfiguration;

    private JMXConnector jmxc;

    private MBeanServerConnection mbeanServerConn;

    private Set<MetricsMBean> mbeans = new HashSet<>();

    /**
     * Constructor.
     *
     * @param config              metrics configuration
     * @param globalConfiguration Global diagnostics configuration
     */
    public MetricsCollector(final MetricsConfiguration config, final GlobalConfiguration globalConfiguration) {
        this.config = config;
        this.globalConfiguration = globalConfiguration;
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

            for (String packageName : config.metricsPackageNames()) {
                String queryName = String.format("%s:*", packageName);
                mbeans.addAll(filterMBeans(packageName, mbeanServerConn.queryMBeans(new ObjectName(queryName), null)));
            }

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
        final Map<String, String> fields = new HashMap<>();

        for (final MetricsMBean mbean : mbeans) {
            for (final MBeanAttributeInfo attribute : mbean.getMBeanAttributes()) {
                try {
                    final Object value = mbeanServerConn
                            .getAttribute(mbean.getMBean().getObjectName(), attribute.getName());

                    if (value != null) {
                        fields.put(mbean.getMeasurementName() + config.metricsSeparator() + attribute.getName(),
                                value.toString());
                    }

                } catch (Exception e) {
                    logger.error("Exception while reading attribute {} of type {}", attribute.getName(),
                            attribute.getType(), e);
                }
            }
        }

        return Arrays.asList(createMeasurement("metrics", fields));
    }

    private Measurement createMeasurement(final String service, final Map<String, String> fields) {
        final Map<String, String> tags = new HashMap<>(1);
        tags.put("host", globalConfiguration.hostname);
        tags.put("systemName", globalConfiguration.systemName);
        return Measurement.create(service, 0d, System.currentTimeMillis(), TimeUnit.MILLISECONDS, tags, fields);
    }

    private Set<MetricsMBean> filterMBeans(final String packageName, final Set<ObjectInstance> mbeanObjectInstances)
            throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        Set<MetricsMBean> results = new HashSet<MetricsMBean>();
        List<Pattern> patterns = new ArrayList<Pattern>();
        for (String pattern : config.metricsPatterns()) {
            patterns.add(Pattern.compile(pattern));
        }

        for (ObjectInstance objectInstance : mbeanObjectInstances) {
            final MBeanAttributeInfo[] attributes = mbeanServerConn.getMBeanInfo(objectInstance.getObjectName())
                    .getAttributes();
            final List<MBeanAttributeInfo> filteredAttributes = new ArrayList<>();

            for (MBeanAttributeInfo attributeInfo : attributes) {
                try {
                    final Object obj = mbeanServerConn
                            .getAttribute(objectInstance.getObjectName(), attributeInfo.getName());
                    // With trying to parse double we are including all mbean attributes that have a number value.
                    // This is necessary because some of the attributes are defined as type Object and type checking
                    // is not possible in that case.
                    Double.parseDouble(obj.toString());
                    filteredAttributes.add(attributeInfo);
                } catch (Exception e) {
                    // Exception handling is unnecessary because we are skipping this attribute
                }
            }

            MetricsMBean mbean = new MetricsMBean(packageName, config, objectInstance, filteredAttributes);

            boolean matches = false;
            if (patterns.isEmpty()) {
                matches = true;
            }

            for (Pattern pattern : patterns) {
                if (pattern.matcher(mbean.buildMBeanName()).matches()) {
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
        if (config.jmxSslEnabled()) {
            return new SslRMIClientSocketFactory();
        } else {
            return RMISocketFactory.getDefaultSocketFactory();
        }
    }

}
