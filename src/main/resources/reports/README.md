Default set of report queries.

* [churn](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/churn): procedure to compute monthly churn (as a percentage and in USD)
* [conversion](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/conversion): procedure to compute revenue converted per day and billing period
* [mrr](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/mrr): procedure to compute MRR per day and billing period
* [v_report_accounts_summary.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_accounts_summary.ddl): overall number of Non-subscribers vs subscribers (defined as having at least one active bundle)
* [v_report_active_by_product_term_monthly.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_active_by_product_term_monthly.ddl): number of active subscriptions created per day, product and billing period
* [v_report_cancellations_count_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_cancellations_count_daily.ddl): number of cancellations per day and phase
* [v_report_chargebacks_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_chargebacks_daily.ddl): amount of chargebacks reported per day and currency
* [v_report_conversions_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_conversions_daily.ddl): number of conversions (trial to non-trial), per day
* [v_report_invoice_adjustments_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoice_adjustments_daily.ddl): sum of invoices adjustments per day and currency
* [v_report_invoice_item_adjustments_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoice_item_adjustments_daily.ddl): sum of invoices item adjustments per day and currency
* [v_report_invoice_item_credits_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoice_item_credits_daily.ddl): sum of invoices credits per day and currency
* [v_report_invoices_balance_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoices_balance_daily.ddl): sum of invoices balance per day and currency
* [v_report_invoices_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoices_daily.ddl): sum of invoices original amount per day and currency
* [v_report_mrr_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_mrr_daily.ddl): daily MRR, broken down by product
* [v_report_new_accounts_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_new_accounts_daily.ddl): number of created accounts per day
* [v_report_overdue_states_count_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_overdue_states_count_daily.ddl): number of accounts per overdue state, per day
* [v_report_payments_total_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_payments_total_daily.ddl): sum of payments (purchases) per day and currency
* [v_report_refunds_total_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_refunds_total_daily.ddl): sum of refunds per day and currency
* [v_report_trial_starts_count_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_trial_starts_count_daily.ddl): number of new trials, per product and day


Utility tables:

* [calendar.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/calendar.sql): procedure to create a *calendar* table


