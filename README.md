# Cassandra Diagnostics

Monitoring and audit power kit for Apache Cassandra.

[![Build Status](https://travis-ci.org/smartcat-labs/cassandra-diagnostics.svg?branch=master)](https://travis-ci.org/smartcat-labs/cassandra-diagnostics)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.smartcat/cassandra-diagnostics/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.smartcat/cassandra-diagnostics)
[![Download](https://api.bintray.com/packages/smartcat-labs/maven/cassandra-diagnostics/images/download.svg) ](https://bintray.com/smartcat-labs/maven/cassandra-diagnostics/_latestVersion)

## Introduction

Cassandra Diagnostics is an extension for Apache Cassandra server node implemented as Java agent. It uses bytecode instrumentation to augment Cassandra node with additional functionalities.

### Slow Query Reporter

Slow Query Reporter measures the query's execution time and reports queries that are executed slower than the configured threshold. It uses a configurable concrete reporter implementation to report slow queries.

Sloq Query Reporter implementations:

- `io.smartcat.cassandra.diagnostics.report.LogQueryReporter` - LogQueryReporter uses the Cassandra logger system to report slow queries. 

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
# Slow query threshold (in nanoseconds)
slowQueryThreshold: 1000000

# Slow query reporter implementation
reporter: io.smartcat.cassandra.diagnostics.report.LogQueryReporter
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
`LogQueryReporter` repors slow queries in `logs/system.log` at `INFO` level.
The dynamic configuration could be inspected/changed using `jconsole` and connecting to `org.apache.cassandra.service.CassandraDaemon`.

# License and development

Cassandra Diagnostics is licensed under the liberal and business-friendly [Apache Licence, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) and is freely available on GitHub. Cassandra Diagnostics is further released to the repositories of Maven Central and on JCenter. The project is built using [Maven](http://maven.apache.org/). From your shell, cloning and building the project would go something like this:

```
git clone https://github.com/smartcat-labs/cassandra-diagnostics.git
cd cassandra-diagnostics
mvn package
```
