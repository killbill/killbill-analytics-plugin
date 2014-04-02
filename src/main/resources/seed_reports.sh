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


create_report 'accounts_summary' 'Account summary' 'COUNTERS' 'v_report_accounts_summary'
create_report 'active_by_product_term_monthly' 'Monthly active subscriptions' 'TIMELINE' 'v_report_active_by_product_term_monthly'
create_report 'cancellations_count_daily' 'Daily cancellations' 'TIMELINE' 'v_report_cancellations_count_daily'
create_report 'chargebacks_daily' 'Daily chargebacks' 'TIMELINE' 'v_report_chargebacks_daily'
create_report 'conversions_daily' 'Daily conversions' 'TIMELINE' 'v_report_conversions_daily'
create_report 'invoice_adjustments_daily' 'Daily invoice adjustments' 'TIMELINE' 'v_report_invoice_adjustments_daily'
create_report 'invoice_item_adjustments_daily' 'Daily invoice item adjustments' 'TIMELINE' 'v_report_invoice_item_adjustments_daily'
create_report 'invoice_item_credits_daily' 'Daily invoice credits' 'TIMELINE' 'v_report_invoice_item_credits_daily'
create_report 'invoices_balance_daily' 'Daily invoice balance' 'TIMELINE' 'v_report_invoices_balance_daily'
create_report 'invoices_daily' 'Daily invoices' 'TIMELINE' 'v_report_invoices_daily'
create_report 'mrr_daily' 'Daily MRR' 'TIMELINE' 'v_report_mrr_daily'
create_report 'new_accounts_daily' 'Daily new accounts' 'TIMELINE' 'v_report_new_accounts_daily'
create_report 'notifications_per_queue_name_count_daily' 'Daily pending notifications' 'TIMELINE' 'v_report_notifications_per_queue_name_count_daily'
create_report 'notifications_per_queue_name_late_count' 'Late notifications' 'COUNTERS' 'v_report_notifications_per_queue_name_late_count'
create_report 'overdue_states_count_daily' 'Daily overdue states' 'TIMELINE' 'v_report_overdue_states_count_daily'
# create_report 'past_period_rev' 'Daily overdue states' 'TIMELINE' 'v_report_past_period_rev'
create_report 'payment_failure_aborted_daily' 'Daily aborted payments' 'TIMELINE' 'v_report_payment_failure_aborted_daily'
create_report 'payment_failures_daily' 'Daily failed payments' 'TIMELINE' 'v_report_payment_failures_daily'
create_report 'payment_plugin_failure_aborted_daily' 'Daily aborted payments (plugin failure)' 'TIMELINE' 'v_report_payment_plugin_failure_aborted_daily'
create_report 'payment_plugin_failure_daily' 'Daily failed payments (plugin failure)' 'TIMELINE' 'v_report_payment_plugin_failure_daily'
create_report 'payment_success_daily' 'Daily successful payments' 'TIMELINE' 'v_report_payment_success_daily'
create_report 'payments_count' 'Payment summary' 'COUNTERS' 'v_report_payments_count'
create_report 'payments_total_daily' 'Daily Payment' 'TIMELINE' 'v_report_payments_total_daily'
create_report 'product_term_monthly' 'Monthly plans' 'TIMELINE' 'v_report_product_term_monthly'
create_report 'refunds_total_daily' 'Daily refunds' 'TIMELINE' 'v_report_refunds_total_daily'
#create_report 'report_revenue_recognition' 'Revenue recognition' 'TIMELINE' 'v_report_revenue_recognition'
create_report 'trial_starts_count_daily' 'Daily trials' 'TIMELINE' 'v_report_trial_starts_count_daily'



create_report 'system_report_control_tag_no_test' 'Control tags' 'COUNTERS' 'v_system_control_tag_no_test'
create_report 'system_report_notifications_per_queue_name' 'Notification queues' 'TIMELINE' 'v_system_report_notifications_per_queue_name'
create_report 'system_report_notifications_per_queue_name_late' 'Late notifications' 'COUNTERS' 'v_system_report_notifications_per_queue_name_late'
create_report 'system_report_payments' 'Payments status' 'COUNTERS' 'v_system_report_payments'
create_report 'system_report_payments_per_day' 'Daily payments' 'TIMELINE' 'v_system_report_payments_per_day'
