#!/usr/bin/env bash

UNABLE_TO_DOWNLOAD_COMPONENT_EXIT_CODE=40
UNKNOWN_REPORTER_MODULE_EXIT_CODE=41

# Downloads file to specified directory.
#
# Parameters:
#   $1 - URL to desired file.
#   $2 - path to output directory.
#
# Exits:
#   - with $UNABLE_TO_DOWNLOAD_COMPONENT_EXIT_CODE, when download is unsuccessful.
function download() {
    wget -q "$1" --directory-prefix="$2" --progress=bar --show-progress -c

    if [ $? != 0 ]; then
        print_error "Unable to download $1. Exiting."
        exit $UNABLE_TO_DOWNLOAD_COMPONENT_EXIT_CODE
    fi
}

# Downloads cassandra-diagnostics libraries using URLs defined in cassandra-diagnostics-urls.sh to $CASSANDRA_LIB_DIR.
#
# Uses global variables:
#   - $DIAGNOSTICS_CORE_URL
#   - $CASSANDRA_LIB_DIR
#   - $DIAGNOSTICS_CONNECTOR_URL
#   - $DIAGNOSTICS_DRIVER_CONNECTOR_URL
#   - $REPORTER_MODULES
#   - $DIAGNOSTICS_REPORTER_URLS
#
# Exits:
#   - with $UNABLE_TO_DOWNLOAD_COMPONENT_EXIT_CODE, when download error occurs.
#   - with $UNKNOWN_REPORTER_MODULE_EXIT_CODE, when unknown reporter module is found in $REPORTER_MODULES.
function download_diagnostics_libraries() {
    # Download diagnostics core.
    download "$DIAGNOSTICS_CORE_URL" "$CASSANDRA_LIB_DIR"

    # Download diagnostics connector.
    download "$DIAGNOSTICS_CONNECTOR_URL" "$CASSANDRA_LIB_DIR"

    # Download diagnostics driver connector.
    download "$DIAGNOSTICS_DRIVER_CONNECTOR_URL" "$CASSANDRA_LIB_DIR"

    # Download diagnostics reporters.
    for reporter in $REPORTER_MODULES; do
        local reporter_url="${DIAGNOSTICS_REPORTER_URLS[$reporter]}"

        if [ -z $reporter_url ]; then
            print_error "Unknown reporter module \"$reporter\". Exiting"
            exit $UNKNOWN_REPORTER_MODULE_EXIT_CODE
        fi

        download "$reporter_url" "$CASSANDRA_LIB_DIR"
    done
}

# Removes installed cassandra-diagnostics libraries.
function remove_installed_cassandra_diagnostics_libraries() {
    verbose_remove "$CASSANDRA_LIB_DIR"/cassandra-diagnostics-core-*.jar
    verbose_remove "$CASSANDRA_LIB_DIR"/cassandra-diagnostics-connector*.jar
    verbose_remove "$CASSANDRA_LIB_DIR"/cassandra-diagnostics-driver-connector-*.jar
    verbose_remove "$CASSANDRA_LIB_DIR"/cassandra-diagnostics-reporter-*.jar
}

# Removes files or directories, if they exist, and prints pre-formatted message.
#
# Parameters:
#   $@ - file or directory paths to remove.
function verbose_remove() {
    for node in $@; do
        if [ -e "$node" ]; then
            print_info $(rm -rvf "$node")
        fi
    done
}
