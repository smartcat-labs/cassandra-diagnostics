package io.smartcat.cassandra.diagnostics.module.metrics;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectInstance;

/**
 * Metrics mbean class.
 */
public class MetricsMBean {

    private final String name;

    private final String separator;

    private final ObjectInstance mbean;

    private MBeanAttributeInfo[] mbeanAttributes = new MBeanAttributeInfo[0];

    /**
     * Constructor.
     *
     * @param config          metrics configuration
     * @param mbean           mbean object instance
     * @param mbeanAttributes mbean attributes
     */
    public MetricsMBean(final MetricsConfiguration config, final ObjectInstance mbean,
            final MBeanAttributeInfo[] mbeanAttributes) {
        this.name = getMBeanName(config.metricsPackageName(), mbean);
        this.separator = config.metricsSeparator();
        this.mbean = mbean;
        this.mbeanAttributes = mbeanAttributes;
    }

    /**
     * Get metrics mbean name.
     *
     * @return mbean name
     */
    public String getName() {
        return name;
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
     * @return mbean attribute info array
     */
    public MBeanAttributeInfo[] getMBeanAttributes() {
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
        String type = mbean.getObjectName().getKeyProperty("type");
        String path = mbean.getObjectName().getKeyProperty("path");
        String keyspace = mbean.getObjectName().getKeyProperty("keyspace");
        String scope = mbean.getObjectName().getKeyProperty("scope");
        String name = mbean.getObjectName().getKeyProperty("name");

        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(metricsPackageName);
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
        return name;
    }
}
