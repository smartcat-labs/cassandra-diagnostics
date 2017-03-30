# Cassandra Diagnostics Connector for Cassandra 3.0.x

Connector is a module which hooks into the query path and extract information for diagnostics. Bytecode instrumentation is used to augment existing Cassandra code with additional functionality. It uses low priority threads to execute the diagnostics information extraction with minimal performance impact to the target code (Cassandra node or application/driver).

## Tracing Slow Queries

Tracing of queries enables reporting the actual queries for some measurement. Currently only module that supports it is Slow Query Module which is reporting queries above some threshold and you can turn on tracing optionally to view actual query which took longer than configured threshold. In future there might be more modules which use tracing to get the idea which query caused some metric (for example module which is reading too many tombstones, or module which is reading row with number of columns above certain threshold).

When LogReporter for Slow Query Module is used (which reports the measurements and tracing information in Cassandra's log), slow query tracing is displayed like this:

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

  - `queuedEventsOverflowThreshold` - Configured threshold for queue size, above this threshold all events will be dropped until the number of queued events is dropped to `queuedEventsRelaxThreshold`.
  - `queuedEventsRelaxThreshold` - Lower threshold bound for event queue size. After the queue was previously in overflow state, new events will be queued only when the number of queued events drop below this value.
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
