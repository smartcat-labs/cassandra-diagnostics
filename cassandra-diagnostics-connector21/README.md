# Cassandra Diagnostics Connector for Cassandra 2.1.x

Connector is a module which hooks into the query path and extract information for diagnostics. Bytecode instrumentation is used to augment existing Cassandra code with additional functionality. It uses low priority threads to execute the diagnostics information extraction with minimal performance impact to the target code (Cassandra node or application/driver).

## Tracing Slow Queries

Tracing of slow queries enables reporting the actual queries which took longer to execute based on the configured treshold. The treshold is configured in Slow Query Module. These slow queries are then reporterted via configured reporters and can be furthur processed or displayed on visualization tools' dashboards.

When LogReporter is used (which reports the measurements and tracing information in Cassandra's log), slow query tracing is displayed like this:

```
INFO  [cassandra-diagnostics-connector-0] 2017-03-23 14:23:58,998 LogReporter.java:35 - Measurement SLOW_QUERY [time=1490275438931, value=50.0, tags={host=SmartCat-Inspiron-5559, statementType=SELECT}, fields={sta
tement=select * from typestest where name = ? and choice = ? LIMIT 100, client=/127.0.0.1:58908}]
```

The slow query, in this example is `select * from typestest where name = ? and choice = ? LIMIT 100` and it came from the 127.0.0.1:58908 client. The query is reported because it is above configured slow query treshlod (configuration option: `slowQueryThresholdInMilliseconds` in SlowQueryModule).

## Configuration

The most important configuration options for the connector are the ones that enable connector to connect to Cassandra JMX:

- `jmxHost` - Node JMX host
- `jmxPort` - Node JMX port

If JMX authentication in Cassandra is enabled, `jmxUsername` and `jmxPassword`  must also be configured:

```yaml
  jmxAuthEnabled: true
  jmxUsername: "username" # actual jmx username
  jmxPassword: "password" # actual jmx password
```

Beside that, there are other configuration options:

- `queuedEventsOverflowThreshold` - The number of diagnostics events waiting to be processed that once reached, new events are being dropped until the number of queued events dropped to `queuedEventsRelaxThreshold`.
- `queuedEventsRelaxThreshold` - The number of diagnostics events waiting to be processed that once reached, after the queue previously was in the overflow state, new events are being queued again, until the number of queued events dropped to `queuedEventsOverflowThreshold`.
- `enableTracing` - Whether to enable tracing or not. It is useful for various modules when debugging. Note that this can impact the performance. The idea is that tracing should be turned on only when needed and turned off once it is not needed anymore.

The connector comes with sensible default values:

```yaml
connector:
  jmxHost: "127.0.0.1" # optional
  jmxPort: 7199 # optional
  jmxAuthEnabled: false # optional
  queuedEventsOverflowThreshold: 1000 # optional
  queuedEventsRelaxThreshold: 700 # optional
  enableTracing: false # optional
```
