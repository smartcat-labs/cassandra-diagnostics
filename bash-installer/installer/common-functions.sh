#!/usr/bin/env bash

# Contains functions shared between the rest of the installer scripts.

# Checks if string starts with substring.
#
# Parameters:
#   $1 - substring with which string is expected to start.
#   $2 - string being checked.
function startswith() {
    case $2 in
    "$1"*)
        true
        ;;
    *)
        false
        ;;
    esac
}

# Prints message(s) to stdout, using predefined format.
function print_message() {
    echo "["`date --rfc-3339='seconds'`"]" "$@"
}

function print_info() {
    print_message "[INFO]" "$@"
}

function print_error() {
    print_message "[ERROR]" "$@"
}

function absolute_path_of() {
    echo $(readlink -f "$1")
}

function print_debug() {
    print_message "[DEBUG]" "$@"
}
