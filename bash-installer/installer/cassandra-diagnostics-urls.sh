#!/usr/bin/env bash

# Contains public URLs to cassandra-diagnostics libraries.

DIAGNOSTICS_BASE_URL="https://dl.bintray.com/smartcat-labs/maven/io/smartcat"

DIAGNOSTICS_CORE_URL="$DIAGNOSTICS_BASE_URL/cassandra-diagnostics-core/$CASSANDRA_DIAGNOSTICS_VERSION/cassandra-diagnostics-core-$CASSANDRA_DIAGNOSTICS_VERSION.jar"

DIAGNOSTICS_CONNECTOR_URL="$DIAGNOSTICS_BASE_URL/cassandra-diagnostics-connector$DIAGNOSTICS_CONNECTOR_VERSION/$CASSANDRA_DIAGNOSTICS_VERSION/cassandra-diagnostics-connector$DIAGNOSTICS_CONNECTOR_VERSION-$CASSANDRA_DIAGNOSTICS_VERSION.jar"

DIAGNOSTICS_DRIVER_CONNECTOR_URL="$DIAGNOSTICS_BASE_URL/cassandra-diagnostics-driver-connector/$CASSANDRA_DIAGNOSTICS_VERSION/cassandra-diagnostics-driver-connector-$CASSANDRA_DIAGNOSTICS_VERSION.jar"

declare -A DIAGNOSTICS_REPORTER_URLS

DIAGNOSTICS_REPORTER_URLS=(
["InfluxReporter"]="$DIAGNOSTICS_BASE_URL/cassandra-diagnostics-reporter-influx/$CASSANDRA_DIAGNOSTICS_VERSION/cassandra-diagnostics-reporter-influx-$CASSANDRA_DIAGNOSTICS_VERSION-all.jar" \
["RiemannReporter"]="$DIAGNOSTICS_BASE_URL/cassandra-diagnostics-reporter-riemann/$CASSANDRA_DIAGNOSTICS_VERSION/cassandra-diagnostics-reporter-riemann-$CASSANDRA_DIAGNOSTICS_VERSION-all.jar" \
["TelegrafReporter"]="$DIAGNOSTICS_BASE_URL/cassandra-diagnostics-reporter-telegraf/$CASSANDRA_DIAGNOSTICS_VERSION/cassandra-diagnostics-reporter-telegraf-$CASSANDRA_DIAGNOSTICS_VERSION-all.jar" \
["DatadogReporter"]="$DIAGNOSTICS_BASE_URL/cassandra-diagnostics-reporter-datadog/$CASSANDRA_DIAGNOSTICS_VERSION/cassandra-diagnostics-reporter-datadog-$CASSANDRA_DIAGNOSTICS_VERSION-all.jar" \
["PrometheusReporter"]="$DIAGNOSTICS_BASE_URL/cassandra-diagnostics-reporter-prometheus/$CASSANDRA_DIAGNOSTICS_VERSION/cassandra-diagnostics-reporter-prometheus-$CASSANDRA_DIAGNOSTICS_VERSION-all.jar" \
["KafkaReporter"]="$DIAGNOSTICS_BASE_URL/cassandra-diagnostics-reporter-kafka/$CASSANDRA_DIAGNOSTICS_VERSION/cassandra-diagnostics-reporter-kafka-$CASSANDRA_DIAGNOSTICS_VERSION.jar"
)
