#!/usr/bin/env bash

# Contains functions for managing cassandra-diagnostics JVM options in cassandra-env.sh.

DIAGNOSTICS_JVM_OPTIONS_HEADER="### Begin cassandra-diagnostics-installer managed configuration. DO NOT EDIT! ###"
DIAGNOSTICS_JVM_OPTIONS_FOOTER="#### End cassandra-diagnostics-installer managed configuration. DO NOT EDIT! ####"
DIAGNOSTICS_JVM_OPTIONS_YAML_OPTION="JVM_OPTS=\"\$JVM_OPTS -Dcassandra.diagnostics.config=$COPY_OF_CASSANDRA_DIAGNOSTICS_CONF_FILE_IN_CONF_DIR\""
DIAGNOSTICS_JVM_OPTIONS_AGENT_OPTION="JVM_OPTS=\"\$JVM_OPTS -javaagent:"$(absolute_path_of "$CASSANDRA_LIB_DIR")"/cassandra-diagnostics-core-$CASSANDRA_DIAGNOSTICS_VERSION.jar\""
CASSANDRA_ENV_SCRIPT_BACKUP_EXTENSION=".old"

let INVALID_CONFIGURATION_EXIT_CODE=60
let FAILED_TO_COPY_CONFIGURATION_FILE_EXIT_CODE=61

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

    remove_diagnostics_configuration_from $CASSANDRA_ENV_SCRIPT
    print_info "Old configuration file saved as $CASSANDRA_ENV_SCRIPT""$CASSANDRA_ENV_SCRIPT_BACKUP_EXTENSION"
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

# Finds line number where cassandra-diagnostics YAML configuration is appended to JVM options.
#
# Parameters:
#   $1 - file to search.
#
# Returns:
#   - line number where configuration is appended, if found.
#   - "-1" if configuration is not found.
function find_unmanaged_yaml_configuration_line_in() {
    echo $(find_line_number_in "$1" "Dcassandra.diagnostics.config=")
}

# Finds line number where cassandra-diagnostics agent configuration is appended to JVM options.
#
# Parameters:
#   $1 - file to search.
#
# Returns:
#   - line number where agent configuration is appended, if found.
#   - "-1" if agent configuration is not found.
function find_unmanaged_agent_configuration_line_in() {
    local line_number=$(grep -n -e "-javaagent:.*/cassandra-diagnostics-core-.*.jar" "$1" | cut -f1 -d:)

    if [ -z "$line_number" ]; then
        echo -1
    else
        echo "$line_number"
    fi
}

# Checks if passed file contains unmanaged cassandra-diagnostics JVM options.
#
# Parameters:
#   $1 - file to search.
#
# Returns:
#   - true, if file contains JVM options.
#   - false otherwise
function contains_unmanaged_diagnostics_configuration_in() {
    let local yaml_line=$(find_unmanaged_yaml_configuration_line_in "$1")
    let local agent_line=$(find_unmanaged_agent_configuration_line_in "$1")

    [ $yaml_line -gt -1 ] || [ $agent_line -gt -1 ]
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

# Removes unmanaged cassandra-diagnostics JVM options.
#
# Parameters:
#   $1 - file from which to remove options.
function remove_unmanaged_diagnostics_configuration_from() {
    local cassandra_env_file_path="$1"
    cp "$cassandra_env_file_path" "$cassandra_env_file_path".bak
    let local yaml_line=$(find_unmanaged_yaml_configuration_line_in "$cassandra_env_file_path")

    if [ $yaml_line -gt -1 ]; then
        sed -i"$CASSANDRA_ENV_SCRIPT_BACKUP_EXTENSION" "$yaml_line,$yaml_line d" "$cassandra_env_file_path"
    fi

    let local agent_line=$(find_unmanaged_agent_configuration_line_in "$1")
    if [ $agent_line -gt -1 ]; then
        sed -i"$CASSANDRA_ENV_SCRIPT_BACKUP_EXTENSION" "$agent_line,$agent_line d" "$cassandra_env_file_path"
    fi
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

    echo -e "$DIAGNOSTICS_JVM_OPTIONS_HEADER" >> "$cassandra_env_file_path"
    echo "" >> "$cassandra_env_file_path"
    echo -e "$DIAGNOSTICS_JVM_OPTIONS_YAML_OPTION" >> "$cassandra_env_file_path"
    echo -e "$DIAGNOSTICS_JVM_OPTIONS_AGENT_OPTION" >> "$cassandra_env_file_path"
    echo "" >> "$cassandra_env_file_path"
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

# Finds and removes cassandra-diagnostics JVM options from cassandra-env.sh.
function find_and_remove_diagnostics_configuration_from_cassandra_env() {
    print_info "Searching for installer managed configuration in $CASSANDRA_ENV_SCRIPT_NAME..."
    if contains_diagnostics_options "$CASSANDRA_ENV_SCRIPT"; then
        print_info "Configuration managed by installer detected in $CASSANDRA_ENV_SCRIPT. Removing."
        remove_diagnostics_configuration_from_env_script

        return
    else
        print_info "Installer managed configuration not found."
    fi

    print_info "Searching for manual configuration in $CASSANDRA_ENV_SCRIPT_NAME..."
    if contains_unmanaged_diagnostics_configuration_in "$CASSANDRA_ENV_SCRIPT"; then
        print_warning "Configuration not managed by installer detected in $CASSANDRA_ENV_SCRIPT."
        print_warning "Backing up original file as $CASSANDRA_ENV_SCRIPT.bak."
        print_warning "Removing configuration..."
        remove_unmanaged_diagnostics_configuration_from "$CASSANDRA_ENV_SCRIPT"
        print_warning "Installer removed previous cassandra-diagnostics configuration from $CASSANDRA_ENV_SCRIPT,"
        print_warning "which was most likely manually added."
        print_warning "Please review and correct $CASSANDRA_ENV_SCRIPT if neccessary."

        return
    else
        print_info "Manual configuration not found."
    fi

    print_info "No cassandra-diagnostics configuration detected in $CASSANDRA_ENV_SCRIPT_NAME."
}

# Copies cassandra-diagnostics configuration file to cassandra configuration directory.
#
# Uses global variables:
#   - CASSANDRA_DIAGNOSTICS_CONF_FILE
#   - CASSANDRA_CONF_DIR
#
# Exits:
#   - with $FAILED_TO_COPY_CONFIGURATION_FILE_EXIT_CODE, if configuration file copying fails.
function copy_diagnostics_configuration_to_conf_dir() {
  local absolute_path_of_conf_file="$(absolute_path_of $CASSANDRA_DIAGNOSTICS_CONF_FILE)"
  if [ "$absolute_path_of_conf_file" == "$COPY_OF_CASSANDRA_DIAGNOSTICS_CONF_FILE_IN_CONF_DIR" ]; then
    print_info "Provided diagnostics configuration file is already in Cassandra conf dir. Skipping."
  else
    cp "$(absolute_path_of $CASSANDRA_DIAGNOSTICS_CONF_FILE)" "$CASSANDRA_CONF_DIR" -f > /dev/null
    if [ $? -ne 0 ]; then
      print_error "Failed to copy diagnostics configuration file to Cassandra conf dir. Exiting."

      exit $FAILED_TO_COPY_CONFIGURATION_FILE_EXIT_CODE
    fi
  fi
}
