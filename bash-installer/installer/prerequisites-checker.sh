#!/usr/bin/env bash

# Contains functions for checking prerequisites for running installer.

UNSUPPORTED_BASH_VERSION_EXIT_CODE=20
UNSUPPORTED_GETOPT_VERSION_EXIT_CODE=21
UNSUPPORTED_AWK_VERSION_EXIT_CODE=22
MISSING_BINARY_EXIT_CODE=23

# Checks if bash version is 4 or newer.
#
# Exits:
#   - with $UNSUPPORTED_BASH_VERSION_EXIT_CODE, if bash version is less than 4.
function check_bash_version() {
    local bash_version=$(bash --version | head -1 | cut -d " " -f 4)

    if ! startswith "4." "$bash_version"; then
        print_error "Unsupported bash version: $bash_version. Version 4 or newer is required."

        exit $UNSUPPORTED_BASH_VERSION_EXIT_CODE
    fi
}

# Checks if getopt is an enhanced version.
#
# Exits:
#   - with $UNSUPPORTED_GETOPT_VERSION_EXIT_CODE, if getopt is not an enhanced version.
function check_getopt_version() {
    getopt --test >/dev/null 2>&1

    if [ $? != 4 ]; then
        print_error "Unsupported getopt version. Enhanced version is required."
    fi
}

# Checks if Awk installation is GNU Awk 4 or newer.
#
# Exits:
#   - with $UNSUPPORTED_AWK_VERSION_EXIT_CODE, if Awk version is not supported.
function check_awk_version() {
    local awk_version=$(awk --version | head -1 | cut -d "," -f 1)

    if ! startswith "GNU Awk 4." "$awk_version"; then
        print_error "Unsupported Awk version: $bash_version. GNU Awk version 4 or newer is required."

        exit $UNSUPPORTED_AWK_VERSION_EXIT_CODE
    fi
}

# Checks if binary can be found on $PATH.
#
# Exits:
#   - with $MISSING_BINARY_EXIT_CODE, if binary is not on $PATH.
function check_if_binary_is_in_path() {
    which "$1" >/dev/null 2>&1

    if [ $? != 0 ]; then
        print_error "Cannot find $1 in PATH. Please install it to be able to use installer."

        exit $MISSING_BINARY_EXIT_CODE
    fi
}

function check_prerequisites() {
    check_bash_version
    check_getopt_version
    check_awk_version
    check_if_binary_is_in_path "wget"
    check_if_binary_is_in_path "readlink"
}

function get_wget_version() {
    echo $(wget --version | head -1 | cut -d" " -f 3)
}
