package io.smartcat.cassandra.diagnostics.reporter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Gauge;
import io.smartcat.cassandra.diagnostics.GlobalConfiguration;
import io.smartcat.cassandra.diagnostics.Measurement;

public class PrometheusReporterTest {

    private PrometheusReporter prometheusReporter;

    @Before
    public void initialize() throws NoSuchFieldException, SecurityException, Exception {
        final ReporterConfiguration config = new ReporterConfiguration();
        config.options.put("httpServerHost", "localhost");
        config.options.put("httpServerPort", 1234);
        prometheusReporter = new PrometheusReporter(config, GlobalConfiguration.getDefault());
    }

    @After
    public void cleanUp() {
        prometheusReporter.stop();
    }

    @Test
    public void should_convert_simple_measurement() {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("v2", "abc");

        final Measurement measurement = Measurement.createSimple("test_metric", 909.0, System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, fields);

        prometheusReporter.report(measurement);

        Gauge gauge = prometheusReporter.metricNameGuageMap.get("test_metric");
        List<MetricFamilySamples> prometheusMetric = gauge.collect();
        Assert.assertEquals(1, prometheusMetric.size());
        Assert.assertEquals("test_metric", prometheusMetric.get(0).name);

        double delta = 0.0001;
        Assert.assertEquals(909.0, prometheusMetric.get(0).samples.get(0).value, delta);
        Assert.assertEquals(1, prometheusMetric.get(0).samples.size());
        Assert.assertEquals("tag1", prometheusMetric.get(0).samples.get(0).labelNames.get(0));
        Assert.assertEquals("tag2", prometheusMetric.get(0).samples.get(0).labelNames.get(1));
        Assert.assertEquals("tv1", prometheusMetric.get(0).samples.get(0).labelValues.get(0));
        Assert.assertEquals("tv2", prometheusMetric.get(0).samples.get(0).labelValues.get(1));

    }

    @Test
    public void should_convert_complex_measurement() {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("tag1", "tv1");
        tags.put("tag2", "tv2");

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("v2", "abc");
        fields.put("cpu", "80.7");
        fields.put("mem", "1.2");

        final Measurement measurement = Measurement.createComplex("test_metric", System.currentTimeMillis(),
                TimeUnit.MILLISECONDS, tags, fields);

        prometheusReporter.report(measurement);

        Assert.assertEquals(2, prometheusReporter.metricNameGuageMap.size());

        Gauge gauge1 = prometheusReporter.metricNameGuageMap.get("test_metric:cpu");
        List<MetricFamilySamples> mertic1 = gauge1.collect();
        Assert.assertEquals(1, mertic1.size());

        Assert.assertEquals("test_metric:cpu", mertic1.get(0).name);
        double delta = 0.0001;
        Assert.assertEquals(80.7, mertic1.get(0).samples.get(0).value, delta);
        Assert.assertEquals(1, mertic1.get(0).samples.size());
        Assert.assertEquals("tag1", mertic1.get(0).samples.get(0).labelNames.get(0));
        Assert.assertEquals("tag2", mertic1.get(0).samples.get(0).labelNames.get(1));
        Assert.assertEquals("tv1", mertic1.get(0).samples.get(0).labelValues.get(0));
        Assert.assertEquals("tv2", mertic1.get(0).samples.get(0).labelValues.get(1));

        Gauge gauge2 = prometheusReporter.metricNameGuageMap.get("test_metric:mem");
        List<MetricFamilySamples> mertic2 = gauge2.collect();
        Assert.assertEquals(1, mertic2.size());

        Assert.assertEquals("test_metric:mem", mertic2.get(0).name);
        Assert.assertEquals(1.2, mertic2.get(0).samples.get(0).value, delta);
        Assert.assertEquals(1, mertic2.get(0).samples.size());
        Assert.assertEquals("tag1", mertic2.get(0).samples.get(0).labelNames.get(0));
        Assert.assertEquals("tag2", mertic2.get(0).samples.get(0).labelNames.get(1));
        Assert.assertEquals("tv1", mertic2.get(0).samples.get(0).labelValues.get(0));
        Assert.assertEquals("tv2", mertic2.get(0).samples.get(0).labelValues.get(1));

    }

}
