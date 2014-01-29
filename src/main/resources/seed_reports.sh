#!/usr/bin/env bash

###################################################################################
#                                                                                 #
#                   Copyright 2010-2014 Ning, Inc.                                #
#                                                                                 #
#      Ning licenses this file to you under the Apache License, version 2.0       #
#      (the "License"); you may not use this file except in compliance with the   #
#      License.  You may obtain a copy of the License at:                         #
#                                                                                 #
#          http://www.apache.org/licenses/LICENSE-2.0                             #
#                                                                                 #
#      Unless required by applicable law or agreed to in writing, software        #
#      distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  #
#      WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the  #
#      License for the specific language governing permissions and limitations    #
#      under the License.                                                         #
#                                                                                 #
###################################################################################

KILLBILL_HOST=${KILLBILL_HOST-"127.0.0.1"}
KILLBILL_PORT=${KILLBILL_PORT-"8080"}

KILLBILL_USER=${KILLBILL_USER-"admin"}
KILLBILL_PASSWORD=${KILLBILL_PASSWORD-"password"}
KILLBILL_API_KEY=${KILLBILL_API_KEY-"bob"}
KILLBILL_API_SECRET=${KILLBILL_API_SECRET-"lazar"}

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
         http://$KILLBILL_HOST:$KILLBILL_PORT/plugins/killbill-analytics/reports
}

create_report 'cancellations_per_day' 'Daily cancellations' 'TIMELINE' 'v_cancellations_per_day'
create_report 'chargebacks_per_day' 'Daily ' 'TIMELINE' 'v_chargebacks_per_day'
create_report 'conversions_per_day' 'Daily ' 'TIMELINE' 'v_conversions_per_day'
create_report 'invoice_adjustments_per_day' 'Daily invoice adjustments' 'TIMELINE' 'v_invoice_adjustments_per_day'
create_report 'invoice_item_adjustments_per_day' 'Daily invoice item adjustements' 'TIMELINE' 'v_invoice_item_adjustments_per_day'
create_report 'invoice_item_credits_per_day' 'Daily invoice credits' 'TIMELINE' 'v_invoice_item_credits_per_day'
create_report 'invoices_balance_per_day' 'Daily invoice balances' 'TIMELINE' 'v_invoices_balance_per_day'
create_report 'invoices_per_day' 'Daily invoices' 'TIMELINE' 'v_invoices_per_day'
create_report 'mrr_per_day' 'Daily MRR' 'TIMELINE' 'v_mrr_per_day'
create_report 'new_accounts_per_day' 'Daily new accounts' 'TIMELINE' 'v_new_accounts_per_day'
create_report 'new_trials_last_24_hours' 'Trials last 24 hours' 'TIMELINE' 'v_new_trials_last_24_hours'
create_report 'new_trials_per_day' 'Daily trials' 'TIMELINE' 'v_new_trials_per_day'
create_report 'overdue_states_per_day' 'Daily overdue states' 'TIMELINE' 'v_overdue_states_per_day'
create_report 'payments_per_day' 'Daily payments' 'TIMELINE' 'v_payments_per_day'
create_report 'refunds_per_day' 'Daily refunds' 'TIMELINE' 'v_refunds_per_day'
create_report 'v_revenue_recognition' 'Revenue Recognition' 'TIMELINE' 'v_revenue_recognition'

create_report 'system_report_control_tag_no_test' 'Control tags' 'COUNTERS' 'v_system_control_tag_no_test'
create_report 'system_report_notifications_per_queue_name' 'Notification queues' 'TIMELINE' 'v_system_report_notifications_per_queue_name'
create_report 'system_report_notifications_per_queue_name_late' 'Late notifications' 'COUNTERS' 'v_system_report_notifications_per_queue_name_late'
create_report 'system_report_payments' 'Payments status' 'COUNTERS' 'v_system_report_payments'
create_report 'system_report_payments_per_day' 'Daily payments' 'TIMELINE' 'v_system_report_payments_per_day'
