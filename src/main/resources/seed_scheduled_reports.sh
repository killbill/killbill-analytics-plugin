#!/usr/bin/env bash

KILLBILL_HTTP_PROTOCOL=${KILLBILL_HTTP_PROTOCOL-"http"}
KILLBILL_HOST=${KILLBILL_HOST-"127.0.0.1"}
KILLBILL_PORT=${KILLBILL_PORT-"8080"}

KILLBILL_USER=${KILLBILL_USER-"admin"}
KILLBILL_PASSWORD=${KILLBILL_PASSWORD-"password"}
KILLBILL_API_KEY=${KILLBILL_API_KEY-"bob"}
KILLBILL_API_SECRET=${KILLBILL_API_SECRET-"lazar"}

MYSQL_HOST=${MYSQL_HOST-"127.0.0.1"}
MYSQL_USER=${MYSQL_USER-"root"}
MYSQL_PASSWORD=${MYSQL_PASSWORD-"root"}
MYSQL_DATABASE=${MYSQL_DATABASE-"killbill"}

function install_ddl() {
    local ddl=$1
    echo "Executing $ddl..."
    mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE -e "source $ddl"
}

function create_report() {
    local report=$1
    for r in `find $report -maxdepth 1 -type f -name '*.json' | sort`; do
        echo "Creating report $r..."
        curl \
            -u $KILLBILL_USER:$KILLBILL_PASSWORD \
            -H "X-Killbill-ApiKey:$KILLBILL_API_KEY" \
            -H "X-Killbill-ApiSecret:$KILLBILL_API_SECRET" \
            -H 'Content-Type: application/json' \
            -d @${r} \
            $KILLBILL_HTTP_PROTOCOL://$KILLBILL_HOST:$KILLBILL_PORT/plugins/killbill-analytics/reports
    done
}

# Install the DDL and creates the reports contained in folders passed as script parameters
for p in ${@}; do
    echo -e "\n***** Processing folder ${p}"
	for r in `find $p -maxdepth 1 -type f -name '*.sql' -o -name '*.ddl' -o -name '*.prc' | sort`; do install_ddl $r; done
	create_report $p
done
