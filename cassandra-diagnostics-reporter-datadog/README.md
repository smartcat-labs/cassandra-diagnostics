# Datadog reporter

[Datadog reporter](https://github.com/smartcat-labs/cassandra-diagnostics/blob/dev/cassandra-diagnostics-reporter-datadog/src/main/java/io/smartcat/cassandra/diagnostics/reporter/DatadogReporter.java) sends measurements towards the configured [Datadog agent](https://github.com/DataDog/dd-agent) using UDP transport.

Datadog reporter has the following configuration parameters (that can be specified using `options`):

- _statsDHost_ - Datadog statsd server host name (IP address). This parameter is required.
- _statsDPort_ - Datadog statsd server UDP port number (8125 by default). This parameter is optional.
- _keysPrefix_ - Datadog measurement prefix (empty string by default). This parameter is optional.
- _fixedTags_ - Datadog measurement tags applied to all measurements (default none). This parameter is optional.

Here is an example configuration that uses Datadog reporter:

```
reporters:
  - reporter: io.smartcat.cassandra.diagnostics.reporter.DatadogReporter
    options:
  	  statsDHost: localhost
      statsDPort: 8125 #Optional
      keysPrefix: test #Optional
      fixedTags:
        - tag1:val1
        - tag2:val2
        - tag3:val3

modules:
  - module: io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule
    measurement: slow_query
    options:
      slowQueryThresholdInMilliseconds: 10
    reporters:
      - io.smartcat.cassandra.diagnostics.reporter.DatadogReporter
```