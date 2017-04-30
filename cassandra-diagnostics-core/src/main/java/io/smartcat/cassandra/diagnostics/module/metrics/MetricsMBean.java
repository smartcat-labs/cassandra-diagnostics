package io.smartcat.cassandra.diagnostics.module.metrics;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectInstance;

/**
 * Metrics mbean class.
 */
public class MetricsMBean {

    private static final String METRICS_PACKAGE_SEPARATOR = ".";

    private final String mbeanName;

    private final String measurementName;

    private final ObjectInstance mbean;

    private List<MBeanAttributeInfo> mbeanAttributes = new ArrayList<>(0);

    /**
     * Constructor.
     *
     * @param packageName     metrics package name
     * @param config          metrics configuration
     * @param mbean           mbean object instance
     * @param mbeanAttributes mbean attributes
     */
    public MetricsMBean(final String packageName, final MetricsConfiguration config, final ObjectInstance mbean,
            final List<MBeanAttributeInfo> mbeanAttributes) {
        this.mbeanName = buildMBeanName(packageName, mbean);
        this.measurementName = buildMeasurementName(packageName, mbean, config.metricsSeparator());
        this.mbean = mbean;
        this.mbeanAttributes = mbeanAttributes;
    }

    /**
     * Get comma separated metrics MBean name.
     *
     * @return MBean name
     */
    public String buildMBeanName() {
        return mbeanName;
    }

    /**
     * Get metrics mbean measurement name. Used for reporting metrics.
     *
     * @return mbean measurement name
     */
    public String getMeasurementName() {
        return measurementName;
    }

    /**
     * Get mbean object instance.
     *
     * @return mbean object instance
     */
    public ObjectInstance getMBean() {
        return mbean;
    }

    /**
     * Get mbean attributes.
     *
     * @return mbean attribute info list
     */
    public List<MBeanAttributeInfo> getMBeanAttributes() {
        return mbeanAttributes;
    }

    private String buildMBeanName(final String metricsPackageName, final ObjectInstance mbean) {
        return nameBuilder(metricsPackageName, mbean, METRICS_PACKAGE_SEPARATOR, true);
    }

    private String buildMeasurementName(final String metricsPackageName, final ObjectInstance mbean,
            final String separator) {
        return nameBuilder(metricsPackageName, mbean, separator, false);
    }

    private String nameBuilder(final String metricsPackageName, final ObjectInstance mbean, final String separator,
            final boolean isMBeanName) {
        final String type = mbean.getObjectName().getKeyProperty("type");
        final String path = mbean.getObjectName().getKeyProperty("path");
        final String keyspace = mbean.getObjectName().getKeyProperty("keyspace");
        final String scope = mbean.getObjectName().getKeyProperty("scope");
        final String name = mbean.getObjectName().getKeyProperty("name");
        final String packageName = metricsPackageName.replace(".", separator);

        StringBuilder nameBuilder = new StringBuilder();
        if (isMBeanName) {
            nameBuilder.append(packageName);
            nameBuilder.append(separator);
        }
        nameBuilder.append(type);
        if (path != null && !path.isEmpty()) {
            nameBuilder.append(separator);
            nameBuilder.append(path);
        }
        if (keyspace != null && !keyspace.isEmpty()) {
            nameBuilder.append(separator);
            nameBuilder.append(keyspace);
        }
        if (scope != null && !scope.isEmpty()) {
            nameBuilder.append(separator);
            nameBuilder.append(scope);
        }
        if (name != null && !name.isEmpty()) {
            nameBuilder.append(separator);
            nameBuilder.append(name);
        }

        return nameBuilder.toString();
    }

    @Override
    public String toString() {
        return mbeanName;
    }
}
