#!/usr/bin/env bash

#
# Copyright 2010-2014 Ning, Inc.
# Copyright 2014-2020 Groupon, Inc
# Copyright 2020-2020 Equinix, Inc
# Copyright 2014-2020 The Billing Project, LLC
#
# The Billing Project licenses this file to you under the Apache License, version 2.0
# (the "License"); you may not use this file except in compliance with the
# License.  You may obtain a copy of the License at:
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations
# under the License.
#

HERE=`cd \`dirname $0\`; pwd`

KILLBILL_HTTP_PROTOCOL=${KILLBILL_HTTP_PROTOCOL-"http"}
KILLBILL_HOST=${KILLBILL_HOST-"127.0.0.1"}
KILLBILL_PORT=${KILLBILL_PORT-"8080"}

KILLBILL_USER=${KILLBILL_USER-"admin"}
KILLBILL_PASSWORD=${KILLBILL_PASSWORD-"password"}
KILLBILL_API_KEY=${KILLBILL_API_KEY-"bob"}
KILLBILL_API_SECRET=${KILLBILL_API_SECRET-"lazar"}

DATABASE_TYPE=${DATABASE_TYPE-"mysql"}  # can be mysql or postgresql

MYSQL_HOST=${MYSQL_HOST-"127.0.0.1"}
MYSQL_USER=${MYSQL_USER-"root"}
MYSQL_PASSWORD=${MYSQL_PASSWORD-"root"}
MYSQL_DATABASE=${MYSQL_DATABASE-"killbill"}

POSTGRES_HOST=${POSTGRES_HOST-"127.0.0.1"}
POSTGRES_PORT=${POSTGRES_PORT-"5432"}
POSTGRES_USER=${POSTGRES_USER-"killbill"}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD-"killbill"}
POSTGRES_DATABASE=${POSTGRES_DATABASE-"killbill"}

REPORTS=$HERE/reports
SYSTEM=$HERE/system

function install_ddl() {
    local ddl=$1
    if [[ $DATABASE_TYPE == "postgresql" ]]; then
        # check if pg ddl file exists
        local pg_ddl="${ddl%%.*}-postgresql.${ddl##*.}"
        if [ -f $pg_ddl ]; then ddl=$pg_ddl; fi
        PGPASSWORD=$POSTGRES_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER $POSTGRES_DATABASE -f $ddl
    else
        mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE -e "source $ddl"
    fi
}

function create_report() {
    local report_name=$1
    local report_pretty_name=$2
    local report_type=$3
    local source_table_name=$4

    curl -v \
         -X POST \
         -u $KILLBILL_USER:$KILLBILL_PASSWORD \
         -H "X-Killbill-ApiKey:$KILLBILL_API_KEY" \
         -H "X-Killbill-ApiSecret:$KILLBILL_API_SECRET" \
         -H 'Content-Type: application/json' \
         -d "{\"reportName\": \"$report_name\",
              \"reportPrettyName\": \"$report_pretty_name\",
              \"reportType\": \"$report_type\",
              \"sourceTableName\": \"$source_table_name\"}" \
         $KILLBILL_HTTP_PROTOCOL://$KILLBILL_HOST:$KILLBILL_PORT/plugins/killbill-analytics/reports
}

# Install the DDL - the calendar table needs to be first
install_ddl $REPORTS/calendar.sql

# Dashboard views
install_ddl $REPORTS/accounts_summary/v_report_accounts_summary.ddl
create_report 'accounts_summary' 'Account summary' 'COUNTERS' 'v_report_accounts_summary'

install_ddl $REPORTS/active_by_product_term_monthly/v_report_active_by_product_term_monthly.ddl
create_report 'active_by_product_term_monthly' 'Active subscriptions' 'TIMELINE' 'v_report_active_by_product_term_monthly'

install_ddl $REPORTS/cancellations_daily/v_report_cancellations_daily.ddl
create_report 'cancellations_count_daily' 'Cancellations' 'TIMELINE' 'v_report_cancellations_daily'

install_ddl $REPORTS/chargebacks_daily/v_report_chargebacks_daily.ddl
create_report 'chargebacks_daily' 'Chargebacks' 'TIMELINE' 'v_report_chargebacks_daily'

install_ddl $REPORTS/v_report_conversions_daily.ddl
create_report 'conversions_daily' 'Conversions' 'TIMELINE' 'v_report_conversions_daily'

install_ddl $REPORTS/v_report_invoice_adjustments_daily.ddl
create_report 'invoice_adjustments_daily' 'Invoice adjustments' 'TIMELINE' 'v_report_invoice_adjustments_daily'

install_ddl $REPORTS/v_report_invoice_item_adjustments_daily.ddl
create_report 'invoice_item_adjustments_daily' 'Invoice item adjustments' 'TIMELINE' 'v_report_invoice_item_adjustments_daily'

install_ddl $REPORTS/v_report_invoice_item_credits_daily.ddl
create_report 'invoice_item_credits_daily' 'Invoice credits' 'TIMELINE' 'v_report_invoice_item_credits_daily'

install_ddl $REPORTS/invoices_balance_daily/v_report_invoices_balance_daily.ddl
create_report 'invoices_balance_daily' 'Invoice balance' 'TIMELINE' 'v_report_invoices_balance_daily'

install_ddl $REPORTS/invoices_daily/v_report_invoices_daily.ddl
create_report 'invoices_daily' 'Invoices' 'TIMELINE' 'v_report_invoices_daily'

install_ddl $REPORTS/mrr/v_report_mrr_daily.ddl
create_report 'mrr_daily' 'MRR' 'TIMELINE' 'v_report_mrr_daily'

install_ddl $REPORTS/new_accounts_daily/v_report_new_accounts_daily.ddl
create_report 'new_accounts_daily' 'New accounts' 'TIMELINE' 'v_report_new_accounts_daily'

install_ddl $REPORTS/v_report_overdue_states_count_daily.ddl
create_report 'overdue_states_count_daily' 'Overdue states' 'TIMELINE' 'v_report_overdue_states_count_daily'

install_ddl $REPORTS/payments_total_daily/v_report_payments_total_daily_sub1.ddl
install_ddl $REPORTS/payments_total_daily/v_report_payments_total_daily.ddl
create_report 'payments_total_daily' 'Payment ($ amount)' 'TIMELINE' 'v_report_payments_total_daily'

install_ddl $REPORTS/refunds_total_daily/v_report_refunds_total_daily.ddl
create_report 'refunds_total_daily' 'Refunds' 'TIMELINE' 'v_report_refunds_total_daily'

install_ddl $REPORTS/v_report_trial_starts_count_daily.ddl
create_report 'trial_starts_count_daily' 'Trials' 'TIMELINE' 'v_report_trial_starts_count_daily'

# System views
for r in `find $SYSTEM -type f -name '*.sql' -o -name '*.ddl' -maxdepth 1`; do install_ddl $r; done

create_report 'system_report_control_tag_no_test' 'Control tags' 'COUNTERS' 'v_system_report_control_tag_no_test'
create_report 'system_report_notifications_per_queue_name' 'Notification queues' 'TIMELINE' 'v_system_report_notifications_per_queue_name'
create_report 'system_report_notifications_per_queue_name_late' 'Late notifications' 'COUNTERS' 'v_system_report_notifications_per_queue_name_late'
create_report 'system_report_payments' 'Payments status' 'COUNTERS' 'v_system_report_payments'
create_report 'system_report_payments_per_day' 'Payments' 'TIMELINE' 'v_system_report_payments_per_day'
