# Telegraf Reporter

[Telegraf Reporter](https://github.com/smartcat-labs/cassandra-diagnostics/blob/dev/cassandra-diagnostics-reporter-telegraf/src/main/java/io/smartcat/cassandra/diagnostics/reporter/TelegrafReporter.java) is a [Cassandra Diagnostics]() reporter that sends measurements towards an [Telegraf agent](https://github.com/influxdata/telegraf). It is a basic implementation based on Java NIO framework and does not have external dependencies.

The reporter uses Telegraf's [TCP Listener](https://github.com/influxdata/telegraf/tree/master/plugins/inputs/tcp_listener) input plugin to transport measurements in the `influx` format (Influx line protocol). Telegraf reporter converts every measurement objects as an Inlufx protocol line. It supports multi-value measurements. A single Influx protocl line looks like this:

```
<measurement name>[,<tag1>=<tag1 value>,...] value=<measurement value>[,<field1>=<field1 value>,...] <measurement timestamp>\r\n
```

Telegraf reporter has the following configuration parameters:

- _telegrafHost_ - Telegraf agent's host name or IP address. This parameter is required.
- _telegrafPort_ - Telegraf agent's TCP port (8084 by default). This parameter is optional.

Here is an example configuration that uses Telegraf reporter:

```
reporters:
  - reporter: io.smartcat.cassandra.diagnostics.reporter.TelegrafReporter
    options:
  	  telegrafHost: 127.0.0.1
      telegrafPort: 8084

modules:
  - module: io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule
    measurement: slow_query
    options:
      slowQueryThresholdInMilliseconds: 10
    reporters:
      - io.smartcat.cassandra.diagnostics.reporter.TelegrafReporter
```

Telegraf agent that receives diagnostics measurements should have enabled the TCP Listener plugin in its configuration and to use the `influx` input data format. This is a sample configuration for the plugin:

```
[[inputs.tcp_listener]]
  service_address = ":8084"
  allowed_pending_messages = 10000
  max_tcp_connections = 250
  data_format = "influx"
```
