# Cassandra Diagnostics Connector for Cassandra 3.0.x

Connector is a module which hooks into the query path and extract information for diagnostics. Bytecode instrumentation is used to augment existing Cassandra code with addition functionality. It uses low priority threads to execute the diagnostics information extraction with minimal performance impact to the target code (Cassandra node or application/driver).

## Tracing Slow Queries

Tracing of slow queries enables watching and logging the actual queries which took longer to execute based on the configured treshold. The treshold is configured in Slow Query Module. This tracing is, then, output into the reporters and can be furthur processed or displayed on visualization tools' dashboards.

When used with LogReported (which reports the measurements and tracing information in Cassandra's log), slow query tracing is displayed like this:

```
INFO  [cassandra-diagnostics-connector-0] 2017-03-23 14:23:58,998 LogReporter.java:35 - Measurement SLOW_QUERY [time=1490275438931, value=50.0, tags={host=SmartCat-Inspiron-5559, statementType=SELECT}, fields={sta
tement=select * from typestest where name = ? and choice = ? LIMIT 100, client=/127.0.0.1:58908}]
```

The slow query, in this example is `select * from typestest where name = ? and choice = ? LIMIT 100` and it came from the 127.0.0.1:58908 client [TODO @nikola please confirm that client part is correct)

## Configuration

The most important configuration options for the connector are:

- `jmxHost` - Node JMX host
- `jmxPort` - Node JMX port

If JMX authentication in Cassandra is enabled, `jmxUsername` and `jmxPassword`  must also be configured:

```yaml
  jmxAuthEnabled: true
  jmxUsername: "username" # actual jmx username
  jmxPassword: "password" # actual jmx password
```

Beside that, there are other configuration options:

- `numWorkerThreads` - The number of worker threads that asynchronously process diagnostics events. 
- `queuedEventsOverflowThreshold` - The number of diagnostics events waiting to be processed that once reached, new events are being dropped until the number of queued events dropped to `queuedEventsRelaxThreshold`.
- `queuedEventsRelaxThreshold` - The number of diagnostics events waiting to be processed that once reached, after the queue previously was in the overflow state, new events are being queued again, until the number of queued events dropped to `queuedEventsOverflowThreshold`.
- `enableTracing` - Whether to enable tracing or not. It is useful for various modules when debugging.

The connector comes with sensible default values:

```yaml
connector:
  jmxHost: "127.0.0.1" # optional
  jmxPort: 7199 # optional
  jmxAuthEnabled: false # optional
  numWorkerThreads: 2
  queuedEventsOverflowThreshold: 1000
  queuedEventsRelaxThreshold: 700
  enableTracing: false
```

