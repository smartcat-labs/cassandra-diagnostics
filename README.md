# Cassandra Diagnostics

Monitoring and audit power kit for Apache Cassandra.

[![Build Status](https://travis-ci.org/smartcat-labs/cassandra-diagnostics.svg?branch=master)](https://travis-ci.org/smartcat-labs/cassandra-diagnostics)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.smartcat/cassandra-diagnostics/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.smartcat/cassandra-diagnostics)
[![Download](https://api.bintray.com/packages/smartcat-labs/maven/cassandra-diagnostics/images/download.svg) ](https://bintray.com/smartcat-labs/maven/cassandra-diagnostics/_latestVersion)

## Introduction

Cassandra Diagnostics is an extension for Apache Cassandra server node implemented as Java agent. It uses bytecode instrumentation to augment Cassandra node with additional functionalities.

### Query Reporter

Query Reporter measures the query's execution time and reports queries that are executed slower than the configured threshold. It uses a configurable concrete reporter implementation to report slow queries.

Query Reporter implementations:

- `io.smartcat.cassandra.diagnostics.report.LogQueryReporter` - LogQueryReporter uses the Cassandra logger system to report slow queries.
- `io.smartcat.cassandra.diagnostics.report.RiemannQueryReporter` - RiemannQueryReporter sends query reports as Riemann events.
 

## Configuration

Cassandra Diagnostics can be configured statically, using a configuration file, and dynamically (in runtime) using JMX.

### Static Configuration

Cassandra Diagnostics uses an external configuration file in YAML format. The default name of the config file is `cassandra-diagnostics.yml` and it is expected to be found on the Cassandra classpath. This can be changed using property `cassandra.diagnostics.config`.
For example, the configuration can be set explicitly by changing `cassandra-env.sh` and adding the following line:

```
JVM_OPTS="$JVM_OPTS -Dcassandra.diagnostics.config=some-other-cassandra-diagnostics-configuration.yml"
```

The following is an example of the configuration file:

```
# Slow query threshold (in milliseconds)
slowQueryThresholdInMillisecond: 100

# Log all queries or just slow queries
logAllQueries: false

# Slow query reporter implementation
reporter: io.smartcat.cassandra.diagnostics.report.LogQueryReporter
```

Specific query reporter may require additional configuration options. Those options could be specified using `reporterOptions` property. The following example shows a configuration options in case of `RiemannQueryReporter`:

```
# Slow query threshold
slowQueryThresholdInMillisecond: 100

# Slow query reporter implementation
reporter: io.smartcat.cassandra.diagnostics.report.RiemannQueryReporter

# Reporter specific options
reporterOptions:
    riemannHost: <riemann server host>
```

### Dynamic Configuration

Cassandra Diagnostics exposes configurable properties through Java JMX. Only properties that could be changed/applied in runtime are exposed.
The Diagnostics JMX MXBean could be found under the following object name:

```
package io.smartcat.cassandra.diagnostics.jmx:type=DiagnosticsMXBean
```

## Query Reporters

### Log Query Reporter

`LogQueryReporter` uses the Cassandra's log to report slow queries. Reports are logged at the `INFO` log level in the following pattern:

```
QueryReport [startTime={}, executionTime={}, clientAddress={}, statement={}]
```

Values for `startTime` and `executionTime` are given in milliseconds.


### Riemann Query Reporter

`RiemannQueryReporter` sends query reports as Riemann events towards the configured Riemann server using TCP transport.

Generated Riemann Events looks like the following:

```
host: <originating host name>
service: "queryReport"
state: "ok"
metric: <execution time in milliseconds>
attributes:
  client: <originating client's TCP socket address>
  statement: <executed statement description>
```

`RiemannQueryReporter` has the following configuration parameters (that can be specified using `reporterOptions`):

- _riemannHost_ - Riemann server's host name (IP address). This parameter is required.
- _riemannPort_ - Riemann server's TCP port number (5555 by default). This parameter is optional.
- _riemannBatchSize_ - Riemann client's batch size (10 by default) - the number of Riemann Events that are send in a batch. This parameter is optional.

## Installation

Place the Diagnostics JAR `cassandra-diagnostics-VERSION-dist.jar` into Cassandra's `lib` directory.
Create and place the configuration file `cassandra-diagnostics.yml` into Cassandra's `conf` directory.
Add the following line at the end of `conf/cassandra-env.sh`:

```
JVM_OPTS="$JVM_OPTS -javaagent:$CASSANDRA_HOME/lib/cassandra-diagnostics-VERSION-dist.jar -Dcassandra.diagnostics.config=cassandra-diagnostics.yml"
```

## Usage

Upon Cassandra node start, the Diagnostics agent kicks in and instrument necessary target classes to inject diagnostics additions.
`LogQueryReporter` repors slow queries in `logs/system.log` at `INFO` level.
The dynamic configuration could be inspected/changed using `jconsole` and connecting to `org.apache.cassandra.service.CassandraDaemon`.

## License and development

Cassandra Diagnostics is licensed under the liberal and business-friendly [Apache Licence, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) and is freely available on GitHub. Cassandra Diagnostics is further released to the repositories of Maven Central and on JCenter. The project is built using [Maven](http://maven.apache.org/). From your shell, cloning and building the project would go something like this:

```
git clone https://github.com/smartcat-labs/cassandra-diagnostics.git
cd cassandra-diagnostics
mvn package
```
