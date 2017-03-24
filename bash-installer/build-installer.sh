#!/usr/bin/env bash

# Builds cassandra-diagnostics installer script.

INSTALLER_SCRIPT_NAME="cassandra-diagnostics-installer.sh"
INSTALLER_RUNNER_NAME="installer-runner.sh"
PAYLOAD_DIRECTORY="installer"
PAYLOAD_ARCHIVE_NAME="installer.tar"
PAYLOAD_COMPRESSED_ARCHIVE_NAME="$PAYLOAD_ARCHIVE_NAME.gz"

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
    check_if_binary_is_in_path "tar"
    check_if_binary_is_in_path "gzip"
}

check_prerequisites

if [ -e "$INSTALLER_SCRIPT_NAME" ]; then
    echo "Removing previous installer script."
    rm -f "$INSTALLER_SCRIPT_NAME"
fi

cd "$PAYLOAD_DIRECTORY"
tar cf ../"$PAYLOAD_ARCHIVE_NAME" ./*
cd ..

if [ ! -e "$PAYLOAD_ARCHIVE_NAME" ]; then
    echo "Error: cannot find $PAYLOAD_ARCHIVE_NAME."
    exit 1
fi

gzip "$PAYLOAD_ARCHIVE_NAME"

if [ ! -e "$PAYLOAD_COMPRESSED_ARCHIVE_NAME" ]; then
    echo "Error: cannot find $PAYLOAD_COMPRESSED_ARCHIVE_NAME."
    exit 1
fi

cat "$INSTALLER_RUNNER_NAME" "$PAYLOAD_COMPRESSED_ARCHIVE_NAME" > "$INSTALLER_SCRIPT_NAME"
chmod a+x "$INSTALLER_SCRIPT_NAME"

echo "Installer script \"$INSTALLER_SCRIPT_NAME\" created."

echo "Cleaning up..."
rm -f "$PAYLOAD_COMPRESSED_ARCHIVE_NAME"
echo "Done."
