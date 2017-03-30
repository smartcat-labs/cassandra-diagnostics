# Cassandra Diagnostics Connector for Cassandra Driver

Connector is a module which hooks into the query path and extract information for diagnostics. Bytecode instrumentation is used to augment existing Cassandra code with additional functionality. It uses low priority threads to execute the diagnostics information extraction with minimal performance impact to the target code (Cassandra node or application/driver).

## Configuration

Configuration options for driver connector:

- `numWorkerThreads` - The number of worker threads that asynchronously process diagnostics events.
- `queuedEventsOverflowThreshold` - Configured threshold for queue size, above this threshold all events will be dropped until the number of queued events is dropped to `queuedEventsRelaxThreshold`.
- `queuedEventsRelaxThreshold` - Lower threshold bound for event queue size. After the queue was previously in overflow state, new events will be queued only when the number of queued events drop below this value.

The connector comes with sensible default values:

```yaml
connector:
  numWorkerThreads: 2 # optional
  queuedEventsOverflowThreshold: 1000 # optional
  queuedEventsRelaxThreshold: 700 # optional
```
