# Influx reporter

[InfluxReporter](https://github.com/smartcat-labs/cassandra-diagnostics/blob/dev/cassandra-diagnostics-reporter-influx/src/main/java/io/smartcat/cassandra/diagnostics/reporter/InfluxReporter.java) sends measurements to [Influx database](https://www.influxdata.com/time-series-platform/influxdb/).

Influx DB statement holds name of measurement, tags connected to this measurement, fields and timestamp of measurement in following format:

```
<measurement name>,id=<UUID for this measurement>,statementType=<type of statement> value=<value of measurement> <timestamp of measurement>
```

Influx reporter has the following configuration parameters:

- _influxDbAddress_: InfluxDB endpoint address (http://127.0.0.1:8086 by default). This parameter is optional.
- _influxUsername_: Authentication username. This parameter is optional but should be set together with _influxPassword_.
- _influxPassword_: Authentication passowrd. This parameter is optional but should be set together with _influxUsername_.
- _influxDbName_: Database name (cassandradb by default). This parameter is optional.
- _influxRetentionPolicy_: Retention policy (`default` by default). This parameter is optional.
- _influxPointsInBatch_: Max points in batch before flush (1000 by default). This parameter is optional.
- _influxFlushPeriodInSeconds_: Max period in seconds between time based flush (5 by default). This parameter is optional.

Here is an example configuration that uses Influx reporter:
```
reporters:
  - reporter: io.smartcat.cassandra.diagnostics.reporter.InfluxReporter
    options:
      influxDbAddress: http://127.0.0.1:8086
      influxUsername: admin
      influxPassword: password
      influxDbName: cassandradb
      influxRetentionPolicy: default
      influxPointsInBatch: 1000
      influxFlushPeriodInSeconds: 5

modules:
  - module: io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule
    measurement: slow_query
    options:
      slowQueryThresholdInMilliseconds: 10
    reporters:
      - io.smartcat.cassandra.diagnostics.reporter.TelegrafReporter
```