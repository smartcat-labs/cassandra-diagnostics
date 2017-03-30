# Cassandra Diagnostics Connector for Cassandra Driver

Connector is a module which hooks into the query path and extract information for diagnostics. Bytecode instrumentation is used to augment existing Cassandra code with additional functionality. It uses low priority threads to execute the diagnostics information extraction with minimal performance impact to the target code (Cassandra node or application/driver).

## Configuration

Configuration options for driver connector:

- `numWorkerThreads` - The number of worker threads that asynchronously process diagnostics events. 
- `queuedEventsOverflowThreshold` - The number of diagnostics events waiting to be processed that once reached, new events are being dropped until the number of queued events dropped to `queuedEventsRelaxThreshold`.
- `queuedEventsRelaxThreshold` - The number of diagnostics events waiting to be processed that once reached, after the queue previously was in the overflow state, new events are being queued again, until the number of queued events dropped to `queuedEventsOverflowThreshold`.

The connector comes with sensible default values:

```yaml
connector:
  numWorkerThreads: 2 # optional
  queuedEventsOverflowThreshold: 1000 # optional
  queuedEventsRelaxThreshold: 700 # optional
```
