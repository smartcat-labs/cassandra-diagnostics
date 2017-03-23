#!/usr/bin/env bash

# Contains functions used to parse input arguments.

ERRONEOUS_ARGUMENT_INPUT=10
MANDATORY_ARGUMENT_MISSING=11
CASSANDRA_ENV_SCRIPT_NAME="cassandra-env.sh"
INSTALLER_SCRIPT_NAME="cassandra-diagnostics-installer.sh"
INSTALLER_SHOW_USAGE=0

# Parses input arguments into usable global variables.
function parse_input_arguments() {
    verify_input_arguments "$@"
    create_global_variables_from_input_arguments "$@"
    check_mandatory_parameters_are_set
    create_additional_variables
}

# Creates global variables from input arguments.
#
# Parameters:
#   $@ - input parameters in short or long form.
#
# Creates global variables:
#   - CASSANDRA_CONFIG_DIR (-c|--cassandra-conf-dir): location of Cassandra configuration directory.
#   - CASSANDRA_LIB_DIR (l|--cassandra-lib-dir): location of Cassandra libraries directory.
#   - CASSANDRA_VERSION (-v|--cassandra-version): version of Cassandra database.
#   - CASSANDRA_DIAGNOSTICS_VERSION (-V|--cassandra-diagnostics-version): version of cassandra-diagnostics to use.
#   - CASSANDRA_DIAGNOSTICS_CONF_FILE (-C|--cassandra-diagnostics-conf-file): location of cassandra-diagnostics YAML configuration file.
#   - INSTALLER_SHOW_USAGE (-h|--help): signal that usage message should be shown.
#
# Exits:
#   - with $ERRONEOUS_ARGUMENT_INPUT, if one of argument values is missing.
function create_global_variables_from_input_arguments() {
    while [[ $# -gt 0 ]]
    do
        key="$1"

        case $key in
            -c|--cassandra-conf-dir)
            CASSANDRA_CONF_DIR=$(absolute_path_of "$2")
            shift
            ;;

            -l|--cassandra-lib-dir)
            CASSANDRA_LIB_DIR=$(absolute_path_of "$2")
            shift
            ;;

            -v|--cassandra-version)
            CASSANDRA_VERSION="$2"
            DIAGNOSTICS_CONNECTOR_VERSION=$(get_short_version_format "$CASSANDRA_VERSION")
            shift
            ;;

            -V|--cassandra-diagnostics-version)
            CASSANDRA_DIAGNOSTICS_VERSION="$2"
            shift
            ;;

            -C|--cassandra-diagnostics-conf-file)
            CASSANDRA_DIAGNOSTICS_CONF_FILE=$(absolute_path_of "$2")
            shift
            ;;

            -h|--help)
            echo "FOUND HELP SWITCH"
            INSTALLER_SHOW_USAGE=1
            shift
            ;;

            *)
            # ignore unknown options
            ;;
        esac
        shift
    done
}

# Converts version number to short format. E.g.:
# 2.1.3 -> 21
# 3.0.5 -> 30
# etc.
# 
# Parameters:
#   $1 - version number
#
# Returns:
#   - version number in short format
function get_short_version_format() {
    local numbers_only=$(echo "$1"| sed 's/[.]//g')

    echo ${numbers_only:0:2}
}

# Verifies if input arguments are properly specified, and if argument value(s) is(are) missing.
#
# Parameters:
#   $@ - all input parameters.
#
# Exits:
#   - with $ERRONEOUS_ARGUMENT_INPUT, if argument value is erroneous.
function verify_input_arguments() {
    local short_options=c:l:v:V:C:h
    local long_options=cassandra-conf-dir:,cassandra-lib-dir:cassandra-version:cassandra-diagnostics-version:cassandra-diagnostics-conf-file:help
    local parsed_options=$(getopt --options $short_options --longoptions $long_options --name "$0" -- "$@")

    if [[ $? -ne 0 ]]; then
        exit $ERRONEOUS_ARGUMENT_INPUT
    fi

    # use eval with "$parsed_options" to properly handle the quoting
    eval set -- "$parsed_options"
}

# Checks if all mandatory arguments are defined as global variables after parsing.
#
# Exits:
#   - with $MANDATORY_ARGUMENT_MISSING, if any of mandatory arguments is not defined.
function check_mandatory_parameters_are_set() {
    if [ $INSTALLER_SHOW_USAGE -eq 1 ]; then
        return
    fi

    if [ -z $CASSANDRA_CONF_DIR ]; then
        print_error "Cassandra configuration directory location is not specified."
        exit $MANDATORY_ARGUMENT_MISSING
    fi
    
    if [ -z $CASSANDRA_LIB_DIR ]; then
        print_error "Cassandra library directory location is not specified."
        exit $MANDATORY_ARGUMENT_MISSING
    fi
    
    if [ -z $CASSANDRA_VERSION ]; then
        print_error "Cassandra version is not specified."
        exit $MANDATORY_ARGUMENT_MISSING
    fi
    
    if [ -z $CASSANDRA_DIAGNOSTICS_VERSION ]; then
        print_error "Cassandra diagnostics version is not specified."
        exit $MANDATORY_ARGUMENT_MISSING
    fi
    
    if [ -z $CASSANDRA_DIAGNOSTICS_CONF_FILE ]; then
        print_error "Cassandra diagnostics configuration file location is not specified."
        exit $MANDATORY_ARGUMENT_MISSING
    fi
}

# Creates additional variables from input parameters.
#
# Creates global variables:
#   - CASSANDRA_ENV_SCRIPT
function create_additional_variables() {
    CASSANDRA_ENV_SCRIPT="$CASSANDRA_CONF_DIR/$CASSANDRA_ENV_SCRIPT_NAME"
}
