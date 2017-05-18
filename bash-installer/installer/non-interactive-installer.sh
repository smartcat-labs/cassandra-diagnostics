#!/usr/bin/env bash

# Non-interactive cassandra-diagnostics installer.

FILE_COPYING_ERROR_EXIT_CODE=1

function print_usage() {
    echo "Usage: $1 [OPTION]..."
    echo "Automates installation of cassandra-diagnostics into existing Cassandra"
    echo "installation."
    echo ""
    echo "Mandatory arguments:"
    echo "-c, --cassandra-conf-dir               location of Cassandra configuration"
    echo "                                       directory"
    echo "-l, --cassandra-lib-dir                location of Cassandra library directory"
    echo "-v, --cassandra-version                installed Cassandra version number "
    echo "                                       (e.g 3.0.12, 2.1.4, etc.)"
    echo "-V, --cassandra-diagnostics-version    desired version of cassandra-diagnostics"
    echo "-C, --cassandra-diagnostics-conf-file  location of cassandra-diagnostics"
    echo "                                       configuration (YAML) file"
    echo "-h, --help                             show this message"
    echo ""
    echo "cassandra-diagnostics source code and documentation URL:"
    echo "<https://github.com/smartcat-labs/cassandra-diagnostics>"
}

# Show usage if there are no input parameters.
if [ $# == 0 ]; then
    print_usage "$INSTALLER_SCRIPT_NAME"
    exit 0
fi

# Include functions for restarting Cassandra service.
. "$PAYLOAD_DIRECTORY"/cassandra-service-manager.sh

# Include arguments parser functions.
. "$PAYLOAD_DIRECTORY"/input-arguments-parser.sh

# Parse arguments and create global variables used by installer.
parse_input_arguments "$@"

# Show usage if help input parameter is passed.
if test ${INSTALLER_SHOW_USAGE+1}; then
    print_usage "$INSTALLER_SCRIPT_NAME"
    exit 0
fi

# Include arguments validation functions.
. "$PAYLOAD_DIRECTORY"/input-arguments-validator.sh

# Validate input arguments.
validate_input_arguments

# Include scripts that depend on properly set global variables.
. "$PAYLOAD_DIRECTORY"/diagnostic-library-files-manager.sh
. "$PAYLOAD_DIRECTORY"/cassandra-diagnostics-urls.sh
. "$PAYLOAD_DIRECTORY"/cassandra-diagnostics-jvm-opts-manager.sh

REPORTER_MODULES=$(find_reporters_in "$CASSANDRA_DIAGNOSTICS_CONF_FILE")

print_info "Downloading cassandra-diagnostics libraries..."
download_diagnostics_libraries
print_info "Done downloading."

print_info "Removing existing cassandra-diagnostics libraries."
remove_installed_cassandra_diagnostics_libraries

print_info "Moving cassandra-diagnostics libraries to $CASSANDRA_LIB_DIR..."
move_diagnostics_libraries_to_lib_dir

print_info "Copying cassandra-diagnostics configuration file to $CASSANDRA_CONF_DIR"
copy_diagnostics_configuration_to_conf_dir

find_and_remove_diagnostics_configuration_from_cassandra_env

print_info "Appending cassandra-diagnostics configuration to $CASSANDRA_ENV_SCRIPT_NAME."
append_diagnostics_configuration_to_env_script

restart_cassandra_service
