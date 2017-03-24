#!/usr/bin/env bash

# Installer main entry point.

PAYLOAD_DIRECTORY=$(dirname "$0")

. "$PAYLOAD_DIRECTORY"/common-functions.sh
. "$PAYLOAD_DIRECTORY"/prerequisites-checker.sh

check_prerequisites

print_info "Staring installation of cassandra-diagnostics."

. "$PAYLOAD_DIRECTORY"/non-interactive-installer.sh

print_info "Installation finished."
