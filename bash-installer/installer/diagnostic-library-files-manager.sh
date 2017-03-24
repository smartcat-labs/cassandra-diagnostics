#!/usr/bin/env bash

UNABLE_TO_DOWNLOAD_COMPONENT_EXIT_CODE=40
UNKNOWN_REPORTER_MODULE_EXIT_CODE=41

function set_wget_options() {
    WGET_VERSION=$(get_wget_version)
    let WGET_MAJOR=$(echo $WGET_VERSION | cut -d"." -f1)
    let WGET_MINOR=$(echo $WGET_VERSION | cut -d"." -f2)

    if [ $WGET_MAJOR -ge 1 ] && [ $WGET_MINOR -ge 18 ]; then
        WGET_OPTIONS="--progress=bar --show-progress -q"
    fi
}

# Downloads file to specified directory.
#
# Parameters:
#   $1 - URL to desired file.
#   $2 - path to output directory.
#
# Exits:
#   - with $UNABLE_TO_DOWNLOAD_COMPONENT_EXIT_CODE, when download is unsuccessful.
function download() {
    if test ${WGET_OPTIONS+1}; then
        wget "$1" --directory-prefix="$2" -c $WGET_OPTIONS
    else
        print_info "Downloading $1 ..."
        wget "$1" --directory-prefix="$2" -c -q
    fi

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
    set_wget_options

    # Download diagnostics core.
    download "$DIAGNOSTICS_CORE_URL" "$LIBRARIES_DOWNLOAD_DIR"

    # Download diagnostics connector.
    download "$DIAGNOSTICS_CONNECTOR_URL" "$LIBRARIES_DOWNLOAD_DIR"

    # Download diagnostics driver connector.
    download "$DIAGNOSTICS_DRIVER_CONNECTOR_URL" "$LIBRARIES_DOWNLOAD_DIR"

    # Download diagnostics reporters.
    for reporter in $REPORTER_MODULES; do
        local reporter_url="${DIAGNOSTICS_REPORTER_URLS[$reporter]}"

        if [ -z $reporter_url ]; then
            print_error "Unknown reporter module \"$reporter\". Exiting"
            exit $UNKNOWN_REPORTER_MODULE_EXIT_CODE
        fi

        download "$reporter_url" "$LIBRARIES_DOWNLOAD_DIR"
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

# Moves downloaded cassandra-diagnostics libraries from temporary directory to Cassandra libraries derectory.
#
# Uses global variables:
#   - $CASSANDRA_LIB_DIR
#   - $LIBRARIES_DOWNLOAD_DIR
function move_diagnostics_libraries_to_lib_dir() {
    mv -f "$LIBRARIES_DOWNLOAD_DIR"/* "$CASSANDRA_LIB_DIR"
}
