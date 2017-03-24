#!/usr/bin/env bash

# Contains functions for validating global variables created from user input.

UNSUPPORTED_CASSANDRA_VERSION_EXIT_CODE=50
NON_EXISTING_FILE_EXIT_CODE=51
NON_EXISTING_DIRECTORY_EXIT_CODE=52
NON_WRITEABLE_NODE_EXIT_CODE=53

# Validates mandatory global variables created from user input.
#
# Uses global variables:
#   - $CASSANDRA_DIAGNOSTICS_CONF_FILE
#   - $CASSANDRA_ENV_SCRIPT
#   - $CASSANDRA_CONF_DIR
#   - $CASSANDRA_LIB_DIR
#
# Exits:
#   - With any of defined exit codes, depending on invalid input.
#     Check used functions for more details.
function validate_input_arguments() {
    check_supported_cassandra_version
    check_if_file_exists "$CASSANDRA_DIAGNOSTICS_CONF_FILE"
    check_if_file_exists "$CASSANDRA_ENV_SCRIPT"
    check_if_directory_exists "$CASSANDRA_CONF_DIR"
    check_if_directory_exists "$CASSANDRA_LIB_DIR"
    check_if_node_is_writable "$CASSANDRA_LIB_DIR"
    check_if_node_is_writable "$CASSANDRA_ENV_SCRIPT"
}

# Checks if provided Cassandra version is supported.
#
# Exits:
#   - with $UNSUPPORTED_CASSANDRA_VERSION_EXIT_CODE, when Cassandra version is unsupported.
function check_supported_cassandra_version() {
    if ! $(startswith "2.1" "$CASSANDRA_VERSION") &&  ! $(startswith "3.0" "$CASSANDRA_VERSION") ; then
        print_error "Unsupported Cassandra version."
        print_error "Supported versions are 2.1.x or 3.0.x."

        exit $UNSUPPORTED_CASSANDRA_VERSION_EXIT_CODE
    fi
}

# Checks if file exists.
#
# Parameters:
#   $1 - path to file to be checked.
#
# Exits:
#   - with NON_EXISTING_FILE_EXIT_CODE, if file does not exist.
function check_if_file_exists() {
    if [ ! -f "$1" ]; then
        print_error "File $1 does not exist."

        exit $NON_EXISTING_FILE_EXIT_CODE
    fi
}

# Checks if directory exists.
#
# Parameters:
#   $1 - path to directory to be checked.
#
# Exits:
#   - with NON_EXISTING_DIRECTORY_EXIT_CODE, if directory does not exist.
function check_if_directory_exists() {
    if [ ! -d "$1" ]; then
        print_error "Directory $1 does not exist."

        exit $NON_EXISTING_DIRECTORY_EXIT_CODE
    fi
}

# Checks if filesystem node is writable.
#
# Parameters:
#   $1 - path to filesystem node to be checked.
#
# Exits:
#   - with NON_WRITEABLE_NODE_EXIT_CODE, if directory is not writable.
function check_if_node_is_writable() {
    if [ ! -w "$1" ]; then
        print_error "$1 is not writeable. Do you have correct permissions?"

        exit $NON_WRITEABLE_NODE_EXIT_CODE
    fi
}
