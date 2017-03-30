#!/usr/bin/env bash

# Extracts payload directory and starts installer entry script.

TMPDIR=$(mktemp -d /tmp/cassandra-diagnostics-installer.XXXXXX)

INSTALLER_ARCHIVE=`awk '/^__INSTALLER_ARCHIVE__/ {print NR + 1; exit 0; }' $0`

tail -n+$INSTALLER_ARCHIVE $0 | tar xz -C $TMPDIR > /dev/null

"$TMPDIR"/main.sh $@
rm -rf $TMPDIR

exit 0

# Installer archive will be appended here as binary.
__INSTALLER_ARCHIVE__
