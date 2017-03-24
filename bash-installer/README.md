# linux-installer

Self-extracting installer used to install cassandra-diagnostics.

# Dependencies

This installer should run on any fairly modern linux distribution. 

Requirements for running installer script:

* bash 4.x
* GNU Awk 4.x
* getopt, enhanced version
* readline
* wget
* mktemp

Requirements for building installer script:

* cat
* tar
* gzip

# Usage

Current implementation is non-interactive, and supports following input switches:

* _-c, --cassandra-conf-dir_               - location of Cassandra configuration directory
* _-l, --cassandra-lib-dir_                - location of Cassandra library directory
* _-v, --cassandra-version_                - installed Cassandra version number (e.g 3.0.12, 2.1.4, etc.)
* _-V, --cassandra-diagnostics-version_    - desired version of cassandra-diagnostics
* _-C, --cassandra-diagnostics-conf-file_  - location of cassandra-diagnostics configuration (YAML) file
* _-h, --help_                             - show help message

All parameters except "-h, --help" are mandatory. 

When invoked with all of mandatory parameters, installer will extract itself to _**/tmp/cassandra-diagnostics-installer.XXXXXX**_ (XXXXXX is random alphanumeric string), and execute _**main.sh**_ script, which will carry on the installation.

# Building self-extracting installer script

Run _**build-installer.sh**_. It will tar and gzip scripts from _**installer**_ directory, and append it at the end of _**installer-runner.sh**_, producing _**cassandra-diagnostics-installer.sh**_ script.
