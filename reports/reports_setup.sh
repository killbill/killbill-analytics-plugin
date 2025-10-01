#!/usr/bin/env bash

#
# Copyright 2020-2025 Equinix, Inc
# Copyright 2014-2025 The Billing Project, LLC
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

MYSQL_HOST=${MYSQL_HOST-"127.0.0.1"}
MYSQL_USER=${MYSQL_USER-"root"}
MYSQL_PASSWORD=${MYSQL_PASSWORD-"killbill"}
MYSQL_DATABASE=${MYSQL_DATABASE-"killbill"}
INSTALL_DDL=true
DROP_EXISTING_REPORT=false

REPORTS=$HERE

function install_ddl() {
    local ddl=$1
    mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE -e "source $ddl"
}

install_all_ddls() {
    echo "Installing DDLs from utils directory first..."
    (
        cd "$REPORTS" || exit 1

        # Process utils first if it exists
        if [ -d "./utils" ]; then
            for ddl_pattern in "v_report_*.ddl" "report_*.ddl" "*.ddl" "*.sql"; do
                shopt -s nullglob
                for ddl in ./utils/$ddl_pattern; do
                    [ -f "$ddl" ] && install_ddl "$ddl"
                done
                shopt -u nullglob
            done
        fi

        echo "Installing other DDLs..."
        # Loop over all directories except utils
        find . -type d ! -path "./utils" -print0 | while IFS= read -r -d '' dir; do
            for ddl_pattern in "v_report_*.ddl" "report_*.ddl"; do
                shopt -s nullglob
                for ddl in "$dir"/$ddl_pattern; do
                    [ -f "$ddl" ] && install_ddl "$ddl"
                done
                shopt -u nullglob
            done
        done
    )
}



function create_report() {
    local report_name=$1
    local report_pretty_name=$2
    local report_type=$3
    local source_table_name=$4
    local refresh_procedure_name=$5

if [[ "$DROP_EXISTING_REPORT" == "true" ]]; then

    curl -v \
         -X DELETE \
         -u "$KILLBILL_USER:$KILLBILL_PASSWORD" \
         -H "X-Killbill-ApiKey:$KILLBILL_API_KEY" \
         -H "X-Killbill-ApiSecret:$KILLBILL_API_SECRET" \
         "http://127.0.0.1:8080/plugins/killbill-analytics/reports/$report_name"
fi


    curl -v \
         -X POST \
         -u $KILLBILL_USER:$KILLBILL_PASSWORD \
         -H "X-Killbill-ApiKey:$KILLBILL_API_KEY" \
         -H "X-Killbill-ApiSecret:$KILLBILL_API_SECRET" \
         -H 'Content-Type: application/json' \
         -d "{\"reportName\": \"$report_name\",
              \"reportPrettyName\": \"$report_pretty_name\",
              \"reportType\": \"$report_type\",
              \"sourceTableName\": \"$source_table_name\",
              \"refreshProcedureName\": \"$refresh_procedure_name\",
              \"refreshFrequency\": \"HOURLY\"
              }" \
         $KILLBILL_HTTP_PROTOCOL://$KILLBILL_HOST:$KILLBILL_PORT/plugins/killbill-analytics/reports
}

# Create all Killbill reports
create_all_reports() {
    declare -a reports=(
        "accounts_summary|Account Summary|TABLE|report_accounts_summary|refresh_report_accounts_summary"
        "active_by_product_term_monthly|Active Subscriptions|TIMELINE|report_active_by_product_term_monthly|refresh_report_active_by_product_term_monthly"
        "bundles_summary|Bundles Summary|TABLE|report_bundles_summary|refresh_report_bundles_summary"
        "cancellations_daily|Cancellations Daily|TIMELINE|report_cancellations_daily|refresh_report_cancellations_daily"
        "chargebacks_daily|Chargebacks Daily|TIMELINE|report_chargebacks_daily|refresh_report_chargebacks_daily"
        "churn_percent|Churn Percent|TIMELINE|report_churn_percent|refresh_report_churn_percent"
        "churn_amount|Churn Total USD|TIMELINE|report_churn_total_usd|refresh_report_churn_total_usd"
        "conversion-total-dollar-amount|Conversions Daily|TIMELINE|report_conversions_daily|refresh_report_conversions_daily"
        "invoice_aging|Invoice Aging|TABLE|report_invoice_aging|refresh_report_invoice_aging"
        "invoice_aging_no_payments_monthly|Invoice Aging No Payments|TABLE|report_invoice_aging_no_payment|refresh_report_invoice_aging_no_payment"
        "invoice_credits_daily|Invoice Credits Daily|TIMELINE|report_invoice_credits_daily|refresh_report_invoice_credits_daily"
        "invoice_credits_monthly|Invoice Credits Monthly|TABLE|report_invoice_credits_monthly|refresh_report_invoice_credits_monthly"
        "invoice_item_adjustments_daily|Invoice Item Adjustments Daily|TIMELINE|report_invoice_item_adjustments_daily|refresh_report_invoice_item_adjustments_daily"
        "invoice_item_adjustments_monthly|Invoice Item Adjustments Monthly|TABLE|report_invoice_item_adjustments_monthly|refresh_report_invoice_item_adjustments_monthly"
        "invoice_items_monthly|Invoice Items Monthly|TABLE|report_invoice_items_monthly|refresh_report_invoice_items_monthly"
        "invoices_balance_daily|Invoice Balance|TIMELINE|report_invoices_balance_daily|refresh_report_invoices_balance_daily"
        "invoices_daily|Invoices Daily|TIMELINE|report_invoices_daily|refresh_report_invoices_daily"
        "invoices_monthly|Invoices Monthly|TABLE|report_invoices_monthly|refresh_report_invoices_monthly"
        "mrr_daily|MRR|TIMELINE|report_mrr_daily|refresh_report_mrr_daily"
        "new_accounts_daily|New Accounts Daily|TIMELINE|report_new_accounts_daily|refresh_report_new_accounts_daily"
        "overdue_states_count_daily|Overdue States Count|TIMELINE|report_overdue_states_count_daily|refresh_report_overdue_states_count_daily"
        "payment_provider_conversions|Payment Provider Conversions|TABLE|report_payment_provider_conversions|refresh_payment_provider_conversions"
        "payment_provider_errors|Payment Provider Errors|TIMELINE|report_payment_provider_errors|refresh_report_payment_provider_errors"
        "payment_provider_monitor|Payment Provider Monitor|TABLE|report_payment_provider_monitor|refresh_payment_provider_monitor"
        "payments_by_provider|Payments By Provider|TABLE|report_payments_by_provider|refresh_report_payments_by_provider"
        "payments_by_provider_summary_last_24_hr_|Payments By Provider Summary (Last 24hrs)|COUNTERS|report_payments_by_provider_last_24h_summary|refresh_report_payments_by_provider_last_24h_summary"
        "payments_monthly|Payments Monthly|TABLE|report_payments_monthly|refresh_report_payments_monthly"
        "payments_summary|Payments Summary|TABLE|report_payments_summary|refresh_report_payments_summary"
        "payments_total_daily|Payment Total Daily|TIMELINE|report_payments_total_daily|refresh_report_payments_total_daily"
        "refunds_monthly|Refunds Monthly|TABLE|report_refunds_summary|refresh_report_refunds_summary"
        "refunds_total_daily|Refunds Total Daily|TIMELINE|report_refunds_total_daily|refresh_report_refunds_total_daily"
        "subscribers_vs_non_subscribers|Subscribers v/s Non Subscribers|COUNTERS|report_subscribers_vs_non_subscribers|refresh_report_subscribers_vs_non_subscribers"
        "trial_starts_count_daily|Trials Start Count|TIMELINE|report_trial_starts_count_daily|refresh_report_trial_starts_count_daily"
        "trial_to_no_trial_conversions_daily|Trial to No Trial Conversions Daily|TIMELINE|report_trial_to_no_trial_conversions_daily|refresh_report_trial_to_no_trial_conversions_daily"
    )

    for r in "${reports[@]}"; do
        IFS="|" read -r name pretty type source refresh <<< "$r"
        create_report "$name" "$pretty" "$type" "$source" "$refresh"
    done
}

# ========================
# Main Execution
# ========================

if [[ "$INSTALL_DDL" == "true" ]]; then
    install_all_ddls
else
    echo "INSTALL_DDL is not true. Skipping DDL installation."
fi

create_all_reports

