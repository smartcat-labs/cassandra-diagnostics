# Cassandra Diagnostics Connector for Cassandra Driver

Connector is a module which hooks into the query path and extract information for diagnostics. Bytecode instrumentation is used to augment existing Cassandra code with addition functionality. It uses low priority threads to execute the diagnostics information extraction with minimal performance impact to the target code (Cassandra node or application/driver).

## Configuration

The most important configuration options for the connector are the ones that enables connector to connect to Cassandra JMX:

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
  numWorkerThreads: 2 # optional
  queuedEventsOverflowThreshold: 1000 # optional
  queuedEventsRelaxThreshold: 700 # optional
  enableTracing: false # optional
```

Note that slow query tracing is currently not possible with driver connector.
