# Prometheus reporter

Warning: Unlike other popular metrics aggregators, Prometheus server _pulls_ the measurements from hosts that are collecting the measurements. This means that, if not configured properly, it can degrade performance of the system that is being monitored. Note also that is not possible to explicitly set the time of the measurement in the Prometheus client, so there might be small difference between the time when the measurement was created in the monitored system by cassandra-diagnostics and when Prometheus measurement object is created based on the cassandra-diagnostics' Measurement object.

[Prometheus reporter](https://github.com/smartcat-labs/cassandra-diagnostics/blob/prometheus-reporter-poc/cassandra-diagnostics-reporter-prometheus/src/main/java/io/smartcat/cassandra/diagnostics/reporter/PrometheusReporter.java) exposes  measurements on the configured host (`httpServerHost`) and port (`httpServerPort`). Prometheus server, then, scrapes the measurements.

Prometheus reporter has the following configuration parameters (that can be specified using `options`):

- _httpServerHost_ - Prometheus client http server's host address for exposing the measurements. This parameter is required. Note that this address needs to be visible to the Prometheus server.
- _httpServerPort_ - Prometheus client http server's port for exposing the measurements (9091 by default). This parameter is optional.

Here is an example configuration that uses Prometheus reporter:

```
reporters:
  - reporter: io.smartcat.cassandra.diagnostics.reporter.PrometheusReporter
    options:
      httpServerHost: "192.168.34.20"
      httpServerPort: 9091

modules:
  - module: io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateModule
    measurement: requestRate
    options:
      period: 10
      timeunit: SECONDS
    reporters:
      - io.smartcat.cassandra.diagnostics.reporter.LogReporter
      - io.smartcat.cassandra.diagnostics.reporter.PrometheusReporter
```

# How it works

Prometheus reporter transforms the Measurements object to Prometheus' Gauge object like this:
- Diagnostics Measurement `name` property is mapped to Gauge object's `name` property
- Dots (`.`) and dashes (`-`) in measurement names are replaced by underscore (`_`) because of restrictions in Prometheus metric's name.
- Diagnostics Measurement tags are mapped to Gauge labels.
- Diagnostics Complex Measurements are mapped to the multiple Gauge objects - one for each field from Complex Measurement. The name of the Prometheus metric created from Complex Measurement is [metric_name]:[field_key].

For example:

```
Measurement [ name=node_info, type=COMPLEX, value=null, time=1505396325752, timeUnit=MILLISECONDS, tags: {host=cassandra,
systemName=smartcat-cassandra-cluster}, fields: {nativeTransportActive=1, uptimeInSeconds=12822051, thriftActive=0, gossipActive=1, exceptionCount=0} ]
```
is transformed to 5 separate Prometheus metrics, one for each field:

```
node_info:uptimeInSeconds{host="cassandra",instance="192.168.34.20:9091",job="prometheus",systemName="smartcat_cassandra_cluster"}
node_info:uptimeInSeconds{host="cassandra",instance="192.168.34.20:9091",job="prometheus",systemName="smartcat_cassandra_cluster"}
node_info:uptimeInSeconds{host="cassandra",instance="192.168.34.20:9091",job="prometheus",systemName="smartcat_cassandra_cluster"}
node_info:uptimeInSeconds{host="cassandra",instance="192.168.34.20:9091",job="prometheus",systemName="smartcat_cassandra_cluster"}
node_info:uptimeInSeconds{host="cassandra",instance="192.168.34.20:9091",job="prometheus",systemName="smartcat_cassandra_cluster"}
```

Each of the metrics can be graphed separately.


