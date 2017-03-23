## Measurement naming

By default all modules have a default measurement name which can be customized by setting the `measurement` configuration property. Bear in mind that some databases have problems parsing some characters out of box (InfluxDB has issues with `.` character in measurement name).

## Heartbeat Module

Heartbeat module produces messages to provide feedback that the diagnostics agent is loaded and working. Typical usage is with Log Reporter where it produces INFO message in configured intervals.
Default interval is 15 minutes.

#### Configuration

All options are optional with default values as shown in this example.

```
- module: io.smartcat.cassandra.diagnostics.module.heartbeat.HeartbeatModule
  measurement: heartbeat #optional
  options:
    period: 15 #optional
    timeunit: MINUTES #optional
  reporters:
    - io.smartcat.cassandra.diagnostics.reporter.LogReporter
```

## Slow Query Module

Slow Query module is monitoring execution time of each query and if it is above configured threshold it reports the value and query type using configured reporters.

#### Configuration

You can specify slow query threshold (in ms) and list of keyspace.table_name for logging. All options are optional with default threshold of 25ms and all table logging.
There is a slow query counter available that periodically reports the count of slow queries over the configured period.   
Two main options that impact reporting are:
* `slowQueryReportEnabled:` Defines is each slow query will be reported (false by default)
* `slowQueryCountReportEnabled:` Defines if slow query periodic count will be reported (true by default)

```
- module: io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule
  measurement: slow_query #optional
  options:
    slowQueryThresholdInMilliseconds: 25 #optional
    slowQueryReportEnabled: false #optional
    slowQueryCountReportEnabled: true #optional
    slowQueryCountReportPeriod: 1 #optional
    slowQueryCountReportTimeunit: MINUTES #optional
    tablesForLogging: #optional
      - keyspace.table_name
  reporters:
    - io.smartcat.cassandra.diagnostics.reporter.LogReporter
```

## Request Rate Module

Request Rate Module counts request rate of executed queries. Rates are reported for select and upsert statements using configured reporters in configured periods.

#### Configuration

Measurement name is by default `request_rate` with `_update` and `_select` suffixes for upsert and select statements respectively. For precise measurements use a default reporting period of 1 second. If higher reporting period is used reported value represents average requests/second for the reporting period.

```
- module: io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateModule
  measurement: request_rate #optional
  options:
    period: 1 #optional
    timeunit: SECONDS #optional
  reporters:
    - io.smartcat.cassandra.diagnostics.reporter.LogReporter
```

## Metrics Module

Cassandra internal metrics are exposed over JMX. This module collects JMX metrics and ships them using predefined reporters. Metrics package names configuration is the same as a default metrics config reporter uses.

#### Configuration

Minimal configuration requires specifying `metricsPatterns` with required metrics package and class names. These are the measurements being collected and reported and the metrics name pattern is the same one as Cassandra's metrics config reporter uses. All additional properties are optional. 

```
- module: io.smartcat.cassandra.diagnostics.module.metrics.MetricsModule
  options:
    period: 1 #optional
    timeunit: SECONDS #optional
    jmxHost: 127.0.0.1 #optional
    jmxPort: 7199 #optional
    jmxSslEnabled: false #optional
    jmxSslUsername: username #optional, set if ssl enabled
    jmxSslPassword: password #optional, set if ssl enabled
    metricsPackageName: "org.apache.cassandra.metrics" #optional
    metricsSeparator: "_" # optional, metrics measurement name separator
    metricsPatterns:
      - "^org.apache.cassandra.metrics.Cache.+"
      - "^org.apache.cassandra.metrics.ClientRequest.+"
      - "^org.apache.cassandra.metrics.CommitLog.+"
      - "^org.apache.cassandra.metrics.Compaction.+"
      - "^org.apache.cassandra.metrics.ColumnFamily.PendingTasks"
      - "^org.apache.cassandra.metrics.ColumnFamily.ReadLatency"
      - "^org.apache.cassandra.metrics.ColumnFamily.WriteLatency"
      - "^org.apache.cassandra.metrics.ColumnFamily.ReadTotalLatency"
      - "^org.apache.cassandra.metrics.ColumnFamily.WriteTotalLatency"
      - "^org.apache.cassandra.metrics.DroppedMetrics.+"
      - "^org.apache.cassandra.metrics.ReadRepair.+"
      - "^org.apache.cassandra.metrics.Storage.+"
      - "^org.apache.cassandra.metrics.ThreadPools.+"
  reporters:
    - io.smartcat.cassandra.diagnostics.reporter.LogReporter
```

## Status Module

Status module is used to report Cassandra information exposed over JMX. It reports all values in a context as a single measurement. For example compaction information is reported as a single measurement where all compaction stats are field values. This reduces the amount of measurement being sent but also provides easier graphing of a measurement.

#### Configuration

Compaction info measurement name is by default `compaction_info`.
Thread pool info measurement name is by default `tpstats_info`.
Repair info measurement name is by default `repair_info`.
Reporting period is by default 1 minute and more frequent updates should be considered heavy on the node.

```
- module: io.smartcat.cassandra.diagnostics.module.status.StatusModule
  options:
    period: 1 #optional
    timeunit: MINUTES #optional
    compactionsEnabled: false #optional
    tpStatsEnabled: false #optional
    repairsEnabled: false #optional
```

## Cluster Health Module

Cluster health module reports information about the health of the cluster exposed over JMX. For examples, it reports if nodes see other node(s) as DOWN. It is used to detect short network partitions or node unavailability.
Default reporting interval is set to 10s in order to detect these short outages.

#### Configuration

```
- module: io.smartcat.cassandra.diagnostics.module.health.ClusterHealthModule
  options:
    period: 10 #optional
    timeunit: SECONDS #optional
    numberOfUnreachableNodesEnabled: true #optional
```

