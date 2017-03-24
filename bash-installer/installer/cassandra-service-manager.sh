#!/usr/bin/env bash

# Contains functions to restart Cassandra service.

CASSANDRA_SERVICE_SYSTEMD_NAME="cassandra"
CASSANDRA_SERVICE_UPSTART_NAME="cassandra"

# Restarts Cassandra service using systemd or upstart.
function restart_cassandra_service() {
    RESTART_SUCCESSFULL=0

    print_info "Restarting Cassandra service..."
    if [ $(has_systemd) ]; then
        let STATUS=$(restart_using_systemd);

        if [ $STATUS -eq 0 ]; then RESTART_SUCCESSFULL=1; fi
    fi

    if [ $RESTART_SUCCESSFULL -eq 0 ]; then
        let STATUS=$(restart_using_upstart)

        if [ $STATUS -eq 0 ]; then RESTART_SUCCESSFULL=1; fi
    fi

    if [ $RESTART_SUCCESSFULL -eq 0 ]; then
        print_warning "Failed to restart Cassandra service, please do it manually to be able to use cassandra-diagnostics."

        return
    fi

    print_info "Restarted."
}

function has_systemd() {
    which systemctl > /dev/null
    echo $?
}

function restart_using_systemd() {
    systemd restart "$CASSANDRA_SERVICE_SYSTEMD_NAME" > /dev/null 2>&1
    echo $?
}

function restart_using_upstart() {
    service "$CASSANDRA_SERVICE_UPSTART_NAME" restart >/dev/null 2<&1
    echo $?
}
