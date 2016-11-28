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
     * @param config          metrics configuration
     * @param mbean           mbean object instance
     * @param mbeanAttributes mbean attributes
     */
    public MetricsMBean(final MetricsConfiguration config, final ObjectInstance mbean,
            final List<MBeanAttributeInfo> mbeanAttributes) {
        this.mbeanName = getMBeanName(config.metricsPackageName(), mbean);
        this.measurementName = nameBuilder(config.metricsPackageName(), mbean, config.metricsSeparator());
        this.mbean = mbean;
        this.mbeanAttributes = mbeanAttributes;
    }

    /**
     * Get comma separated metrics mbean name.
     *
     * @return mbean name
     */
    public String getmbeanName() {
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

    /**
     * Get mbean measurement names based on the metrics package naming pattern.
     * <p>
     * Everything is named based on this pattern:
     * <p>
     * type=*, scope=*, name=*
     * type=ThreadPools, path=*, scope=*, name=*
     * type=ColumnFamily, keyspace=*, scope=*, name=*
     * type=Keyspace, keyspace=*, name=*
     *
     * @param metricsPackageName metrics package name
     * @param mbean              MBean object instance
     * @return mbean measurement name
     */
    private String getMBeanName(final String metricsPackageName, final ObjectInstance mbean) {
        return nameBuilder(metricsPackageName, mbean, METRICS_PACKAGE_SEPARATOR);
    }

    private String nameBuilder(final String metricsPackageName, final ObjectInstance mbean, final String separator) {
        final String type = mbean.getObjectName().getKeyProperty("type");
        final String path = mbean.getObjectName().getKeyProperty("path");
        final String keyspace = mbean.getObjectName().getKeyProperty("keyspace");
        final String scope = mbean.getObjectName().getKeyProperty("scope");
        final String name = mbean.getObjectName().getKeyProperty("name");
        final String packageName = metricsPackageName.replace(".", separator);

        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(packageName);
        nameBuilder.append(separator);
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
