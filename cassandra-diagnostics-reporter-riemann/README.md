# Riemann reporter

[Riemann reporter](https://github.com/smartcat-labs/cassandra-diagnostics/blob/dev/cassandra-diagnostics-reporter-riemann/src/main/java/io/smartcat/cassandra/diagnostics/reporter/RiemannReporter.java) sends measurements as Riemann events towards the configured Riemann server using TCP transport. It is using batch Riemann client to save resources and send more events in one network round trip.

Generated Riemann Events looks like the following:

```
host: <originating host name>
service: <measurement name>
state: "ok"
metric: <measurement value>
ttl: 30
tags:
  id: <UUID for this measurement>
  statementType: <type of statement>
fields:
  client: <client doing request>
  statement: <statement which was executed>
```

Riemann reporter has the following configuration parameters (that can be specified using `options`):

- _riemannHost_ - Riemann server's host name (IP address). This parameter is required.
- _riemannPort_ - Riemann server's TCP port number (5555 by default). This parameter is optional.
- _batchEventSize_ - Riemann events that fit in one batch, this is length that triggers sending of events (10 by default). This parameter is optional.

Here is an example configuration that uses Riemann reporter:

```
reporters:
  - reporter: io.smartcat.cassandra.diagnostics.reporter.RiemannReporter
    options:
  	  riemannHost: 127.0.0.1
      riemannPort: 5555 #Optional
      batchEventSize: 50 #Optional

modules:
  - module: io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule
    measurement: slow_query
    options:
      slowQueryThresholdInMilliseconds: 10
    reporters:
      - io.smartcat.cassandra.diagnostics.reporter.RiemannReporter
```