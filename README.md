# Cassandra Diagnostics

Monitoring and audit power kit for Apache Cassandra.

[![Build Status](https://travis-ci.org/smartcat-labs/cassandra-diagnostics.svg?branch=master)](https://travis-ci.org/smartcat-labs/cassandra-diagnostics)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.smartcat/cassandra-diagnostics/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.smartcat/cassandra-diagnostics)
[![Download](https://api.bintray.com/packages/smartcat-labs/maven/cassandra-diagnostics/images/download.svg) ](https://bintray.com/smartcat-labs/maven/cassandra-diagnostics/_latestVersion)

## Introduction

Cassandra Diagnostics is an extension for Apache Cassandra server node implemented as Java agent. It uses bytecode instrumentation to augment Cassandra node with additional functionalities. On one side it has connectors for different versions of Cassandra and on the other it has reporters to send measurement to different tools. In between lies core which is glue between those two. Reusable code goes to commons.

### Cassandra Connector

[Cassandra Connector](https://github.com/smartcat-labs/cassandra-diagnostics/tree/dev/cassandra-diagnostics-connector21) is module which hooks into query lifecycle and extract information for measurement. It uses bytecode instrumentation to augment Cassandra node with addition functionality and it uses low priority thread to measure query performance not to hurt overall performance of Cassandra cluster.

### Cassandra Core

[Cassandra Diagnostics Core](https://github.com/smartcat-labs/cassandra-diagnostics/tree/dev/cassandra-diagnostics-core) is glue between connector and reporters. It holds all the modules for diagnostics, it has business logic for measurement and it decides what will be measured and what would be skipped. Its job is to load provided configuration or to setup sensible defaults.

### Cassandra Diagnostic Commons

[Cassandra Diagnostics Commons](https://github.com/smartcat-labs/cassandra-diagnostics/tree/dev/cassandra-diagnostics-commons) holds interface for core, connector and reports and it provides signature all the modules need to confront to be able to work together.

### Modules

There are default module implementations which serve as core features. Modules use configured reporters to report their activity.

Module implementations:

#### Heartbeat Module

Heartbeat module produces messages to provide feedback that the diagnostics agent is loaded and working. Typical usage is with Log Reporter where it produces INFO message in configured intervals.
Default interval is 15 minutes.

#### Slow Query Module

Slow Query module is monitoring execution time of each query and if it is above configured threshold it reports the value and query type using configured reporters.
Default is 25 milliseconds.

#### Request Rate Module

Request Rate Module uses codahale metrics library to create rate measurement of executed queries. Rates are reported for select and upsert statements using configured reporters in configured periods.
Default is 1 second.

### Reporters

Reporters take measurement from core and wrap them up in implementation specific format so it can be sent to reporters target (i.e. Influx reporter transforms measurement to influx query and stores it to InfluxDB).

Reporter implementations:

#### Log Reporter

[LogReporter}(https://github.com/smartcat-labs/cassandra-diagnostics/blob/dev/cassandra-diagnostics-core/src/main/java/io/smartcat/cassandra/diagnostics/reporter/LogReporter.java) uses the Cassandra logger system to report measurement (this is default reporter and part of core). Reports are logged at the `INFO` log level in the following pattern:

```
Measurement {} [time={}, value={}, tags={}, fields={}]
```

Values for `time` is given in milliseconds. `tags` are used to better specify measurement and provide additional searchable labels and fields is placeholder for additional fields connected to this measurement. Example can be Slow Query measurement, where `value` is execution time of query, `tags` can be type of statement (UPDATE or SELECT) so you can differentiate and search easy and `fields` can hold actual statement, which is not something you want to search against but it is valuable metadata for measurement.

#### Riemann Reporter

[RiemannReporter](https://github.com/smartcat-labs/cassandra-diagnostics/blob/dev/cassandra-diagnostics-reporter-riemann/src/main/java/io/smartcat/cassandra/diagnostics/reporter/RiemannReporter.java) sends measurements as Riemann events towards the configured Riemann server using TCP transport.

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

`RiemannReporter` has the following configuration parameters (that can be specified using `options`):

- _riemannHost_ - Riemann server's host name (IP address). This parameter is required.
- _riemannPort_ - Riemann server's TCP port number (5555 by default). This parameter is optional.

#### Influx Reporter

[InfluxReporter](https://github.com/smartcat-labs/cassandra-diagnostics/blob/dev/cassandra-diagnostics-reporter-influx/src/main/java/io/smartcat/cassandra/diagnostics/reporter/InfluxReporter.java) sendss measurements to influx database.

Influx DB statement holds sname of measurement, tags connected to this measurement, fields and timestamp of measurement in following format:

```
<measurement name>,id=<UUID for this measurement>,statementType=<type of statement> value=<value of measurement> <timestamp of measurement>
```

## Configuration

Cassandra Diagnostics can be configured statically, using a configuration file, and dynamically (in runtime) using JMX.

### Static Configuration

Cassandra Diagnostics uses an external configuration file in YAML format. You can see default configuration in [cassandra-diagnostics-default.yml](https://github.com/smartcat-labs/cassandra-diagnostics/blob/dev/cassandra-diagnostics-core/src/main/resources/cassandra-diagnostics-default.yml). The default name of the config file is `cassandra-diagnostics.yml` and it is expected to be found on the Cassandra classpath. This can be changed using property `cassandra.diagnostics.config`.
For example, the configuration can be set explicitly by changing `cassandra-env.sh` and adding the following line:

```
JVM_OPTS="$JVM_OPTS -Dcassandra.diagnostics.config=some-other-cassandra-diagnostics-configuration.yml"
```

The following is an example of the configuration file:

```
reporters:
  - reporter: io.smartcat.cassandra.diagnostics.reporter.LogReporter

modules:
  - module: io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule
    measurement: queryReport
    options:
      slowQueryThresholdInMilliseconds: 1
    reporters:
      - io.smartcat.cassandra.diagnostics.reporter.LogReporter
```

Specific query reporter may require additional configuration options. Those options could be specified using `options` property. The following example shows a configuration options in case of `RiemannReporter` and `InfluxReporter`. Also it shows how you can configure specific modules:

```
# Reporters
reporters:
  - reporter: io.smartcat.cassandra.diagnostics.reporter.LogReporter
  - reporter: io.smartcat.cassandra.diagnostics.reporter.RiemannReporter
    options:
      riemannHost: 127.0.0.1
      riemannPort: 5555 #Optional
  - reporter: io.smartcat.cassandra.diagnostics.reporter.InfluxReporter
    options:
      influxDbAddress: http://127.0.0.1:8086
      influxUsername: username #Optional
      influxPassword: password #Optional
      influxDbName: cassandradb #Optional
      influxRetentionPolicy: default #Optional

# Modules
modules:
  - module: io.smartcat.cassandra.diagnostics.module.heartbeat.HeartbeatModule
    options:
      period: 15
      timeunit: MINUTES
    reporters:
      - io.smartcat.cassandra.diagnostics.reporter.LogReporter
  - module: io.smartcat.cassandra.diagnostics.module.slowquery.SlowQueryModule
    measurement: queryReport
    options:
      # Slow query threshold
      slowQueryThresholdInMilliseconds: 25
  - module: io.smartcat.cassandra.diagnostics.module.requestrate.RequestRateModule
    measurement: requestRate
    options:
      period: 1
      timeunit: SECONDS
      separateByRequestType: true
    reporters:
      - io.smartcat.cassandra.diagnostics.reporter.LogReporter
      - io.smartcat.cassandra.diagnostics.reporter.InfluxReporter
```

### Dynamic Configuration

Cassandra Diagnostics exposes configurable properties through Java JMX. Only properties that could be changed/applied in runtime are exposed.
The Diagnostics JMX MXBean could be found under the following object name:

```
package io.smartcat.cassandra.diagnostics.jmx:type=DiagnosticsMXBean
```

## Installation

Place the Diagnostics JAR `cassandra-diagnostics-VERSION-dist.jar` into Cassandra's `lib` directory.
Create and place the configuration file `cassandra-diagnostics.yml` into Cassandra's `conf` directory.
Add the following line at the end of `conf/cassandra-env.sh`:

```
JVM_OPTS="$JVM_OPTS -javaagent:$CASSANDRA_HOME/lib/cassandra-diagnostics-VERSION-dist.jar -Dcassandra.diagnostics.config=cassandra-diagnostics.yml"
```

## Usage

Upon Cassandra node start, the Diagnostics agent kicks in and instrument necessary target classes to inject diagnostics additions.
`LogReporter` repors slow queries in `logs/system.log` at `INFO` level.
The dynamic configuration could be inspected/changed using `jconsole` and connecting to `org.apache.cassandra.service.CassandraDaemon`.

## License and development

Cassandra Diagnostics is licensed under the liberal and business-friendly [Apache Licence, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) and is freely available on GitHub. Cassandra Diagnostics is further released to the repositories of Maven Central and on JCenter. The project is built using [Maven](http://maven.apache.org/). From your shell, cloning and building the project would go something like this:

```
git clone https://github.com/smartcat-labs/cassandra-diagnostics.git
cd cassandra-diagnostics
mvn package
```
