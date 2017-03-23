#!/usr/bin/env bash

# Contains functions for managing cassandra-diagnostics JVM options in cassandra-env.sh.

DIAGNOSTICS_JVM_OPTIONS_HEADER="### Begin cassandra-diagnostics-installer managed configuration. DO NOT EDIT! ###"
DIAGNOSTICS_JVM_OPTIONS_FOOTER="#### End cassandra-diagnostics-installer managed configuration. DO NOT EDIT! ####"
DIAGNOSTICS_JVM_OPTIONS_TEMPLATE="JVM_OPTS=\"\$JVM_OPTS -Dcassandra.diagnostics.config=\""
CASSANDRA_ENV_SCRIPT_BACKUP_EXTENSION=".old"

INVALID_CONFIGURATION_EXIT_CODE=60

# Appends cassandra-diagnostics JVM options to end of cassandra-env.sh script.
#
# Uses global variables:
#   - $CASSANDRA_ENV_SCRIPT
#   - $CASSANDRA_DIAGNOSTICS_CONF_FILE
function append_diagnostics_configuration_to_env_script() {
    local absolute_conf_file_path=$(absolute_path_of "$CASSANDRA_DIAGNOSTICS_CONF_FILE")

    append_managed_configuration "$CASSANDRA_ENV_SCRIPT" "$absolute_conf_file_path"
}

# Removes old cassandra-diagnostics JVM options from cassandra-env.sh script, if present
#
# Uses global variables:
#   - $CASSANDRA_ENV_SCRIPT
#   - $CASSANDRA_ENV_SCRIPT_BACKUP_EXTENSION
#   - $CASSANDRA_DIAGNOSTICS_CONF_FILE
function remove_diagnostics_configuration_from_env_script() {
    local absolute_conf_file_path=$(absolute_path_of "$CASSANDRA_DIAGNOSTICS_CONF_FILE")

    if contains_diagnostics_options "$CASSANDRA_ENV_SCRIPT"; then
        print_info "Managed configuration detected. Removing..."
        remove_diagnostics_configuration_from $CASSANDRA_ENV_SCRIPT
        print_info "Old configuration file saved as $CASSANDRA_ENV_SCRIPT""$CASSANDRA_ENV_SCRIPT_BACKUP_EXTENSION"
    else
        print_info "Existing managed configuration not detected."
    fi
}

# Checks if cassandra-env.sh contains old cassandra-diagnostics JVM options.
#
# Parameters:
#   $1 - path to cassandra-env.sh file.
#
# Returns:
#   - true if it contains cassandra-diagnostics JVM options,
#     false otherwise.
function contains_diagnostics_options() {
    let local start_line=$(find_header_line_number_in "$1")
    let local end_line=$(find_footer_line_number_in "$1")

    if [ $start_line -eq -1 ] && [ $end_line -eq -1 ]; then
        return 1 # false
    fi

    if [ $start_line -eq -1 ] && [ $end_line -gt 0 ]; then
        exit_with_invalid_configuration
    fi

    if [ $end_line -eq -1 ] && [ $start_line -gt 0 ]; then
        exit_with_invalid_configuration
    fi

    if [ $start_line -ge $end_line ]; then
        exit_with_invalid_configuration
    fi

    [ -n "$start_line" ] && [ -n "$end_line" ]
}

function exit_with_invalid_configuration() {
    print_error "Cassandra-diagnostics configuration in $1 seems to be invalid."
    print_error "Please fix it manually and re-run installer."

    exit $INVALID_CONFIGURATION_EXIT_CODE
}

# Finds line number where cassandra-diagnostics JVM options header is in input file.
#
# Parameters:
#   $1 - file to search for header.
#
# Returns:
#   - line number where header is, if found.
#   - "-1" if header is not found.
function find_header_line_number_in() {
    echo $(find_line_number_in "$1" "$DIAGNOSTICS_JVM_OPTIONS_HEADER")
}

# Finds line number where cassandra-diagnostics JVM options footer is in input file.
#
# Parameters:
#   $1 - file to search for footer.
#
# Returns:
#   - line number where footer is, if found.
#   - "-1" if footer is not found.
function find_footer_line_number_in() {
    echo $(find_line_number_in "$1" "$DIAGNOSTICS_JVM_OPTIONS_FOOTER")
}

# Finds line number of provided string in provided file.
#
# Parameters:
#   $1 - file to search.
#   $2 - string being searched.
#
# Returns:
#   - line number where string is, if found.
#   - "-1" if string is not found.
function find_line_number_in() {
    local line_number=$(grep -n "$2" "$1" | cut -f1 -d:)

    if [ -z "$line_number" ]; then
        echo -1
    else
        echo "$line_number"
    fi
}

# Removes cassandra-diagnostics JVM options from provided file.
#
# Parameters:
#   $1 - file from which to remove options.
function remove_diagnostics_configuration_from() {
    let local start_line=$(find_header_line_number_in "$1")
    let local end_line=$(find_footer_line_number_in "$1")

    delete_lines_from "$1" $start_line $end_line
}

# Deletes lines from file.
#
# Parameters:
#   $1 - file from which to delete lines.
#   $2 - start line number.
#   $3 - end line number.
function delete_lines_from() {
    local cassandra_env_file_path="$1"
    local start_line="$2"
    local end_line="$3"

    sed -i"$CASSANDRA_ENV_SCRIPT_BACKUP_EXTENSION" "$start_line,$end_line d" "$cassandra_env_file_path"
}

# Appends cassandra-diagnostics JVM options to provided file.
#
# Parameters:
#   $1 - file to append JVM options to.
#   $2 - absolute path to cassandra-diagnostics configuration file.
function append_managed_configuration() {
    local cassandra_env_file_path="$1"
    local absolute_conf_file_path="$2"

    echo -e "$DIAGNOSTICS_JVM_OPTIONS_HEADER \n" >> "$cassandra_env_file_path"
    echo -e "$DIAGNOSTICS_JVM_OPTIONS_TEMPLATE""$absolute_conf_file_path \n" >> "$cassandra_env_file_path"
    echo -e "$DIAGNOSTICS_JVM_OPTIONS_FOOTER" >> "$cassandra_env_file_path"
}

# Finds reporter classes in cassandra-diagnostics configuration file.
#
# Parameters:
#   $1 - absolute path to cassandra-diagnostics configuration file.
#
# Returns:
#   - string containing space-separated list of reporter classes.
function find_reporters_in() {
    echo $(awk -f "$PAYLOAD_DIRECTORY"/find-reporter-modules.awk "$1")
}
