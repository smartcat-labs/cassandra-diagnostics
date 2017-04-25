# Kafka reporter

[KafkaReporter](src/main/java/io/smartcat/cassandra/diagnostics/reporter/KafkaReporter.java) sends measurements to a [Kafka](https://kafka.apache.org/) topic.

Measurements objects are sent do designated Kafka topic. The message payload is JSON-serialized Measurement object and the message key is the hostname (serialized by Kafka's StringSerializer).

Kafka reporter has the following configuration parameters:

- _kafkaBootstrapServers_: A list of Kafka cluster bootstrap servers given as _host:port_. This parameter is mandatory.
- _kafkaTopic_: The name of the destination Kafka topic. This parameter is mandatory.

Here is an example configuration that uses Kafka reporter:

```
reporters:
  - reporter: io.smartcat.cassandra.diagnostics.reporter.KafkaReporter
    options:
      kafkaBootstrapServers: 10.0.0.20:9092,10.0.0.21:9092
      kafkaTopic: measurements

modules:
  - module: io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule
    measurement: slow_query
    options:
      slowQueryThresholdInMilliseconds: 10
    reporters:
      - io.smartcat.cassandra.diagnostics.reporter.KafkaReporter
```
