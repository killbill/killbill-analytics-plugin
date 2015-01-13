Default set of report queries.

* [calendar.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/calendar.sql): procedure to create a utility *calendar* table
* [invoices_balance_per_day.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/invoices_balance_per_day.sql): sum of invoices balance, per currency and day
* [mrr_per_day.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/mrr_per_day.sql): daily MRR, broken down by product
* [overdue_states_per_day.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/overdue_states_per_day.sql): number of accounts per overdue state, per day
* [v_report_accounts_summary.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_accounts_summary.ddl): overall number of Non-subscribers vs subscribers (defined as having at least one active bundle)
* [v_report_active_by_product_term_monthly.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_active_by_product_term_monthly.ddl): number of active subscriptions created per day, product and billing period
* [v_report_cancellations_count_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_cancellations_count_daily.ddl): number of cancellations per day and phase
* [v_report_chargebacks_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_chargebacks_daily.ddl): amount of chargebacks reported per day and currency
* [v_report_conversions_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_conversions_daily.ddl): number of conversions (trial to non-trial), per day
* [v_report_invoice_adjustments_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoice_adjustments_daily.ddl): amount of invoice adjustments per day and currency
* [v_report_invoice_item_adjustments_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoice_item_adjustments_daily.ddl): 
* [v_report_invoice_item_credits_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoice_item_credits_daily.ddl): 
* [v_report_invoices_balance_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoices_balance_daily.ddl): 
* [v_report_invoices_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_invoices_daily.ddl): 
* [v_report_mrr_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_mrr_daily.ddl): 
* [v_report_new_accounts_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_new_accounts_daily.ddl): 
* [v_report_notifications_per_queue_name_count_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_notifications_per_queue_name_count_daily.ddl): 
* [v_report_notifications_per_queue_name_late_count.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_notifications_per_queue_name_late_count.ddl): 
* [v_report_overdue_states_count_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_overdue_states_count_daily.ddl): 
* [v_report_past_period_rev.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_past_period_rev.ddl): 
* [v_report_payment_failure_aborted_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_payment_failure_aborted_daily.ddl): 
* [v_report_payment_failure_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_payment_failure_daily.ddl): 
* [v_report_payment_failures_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_payment_failures_daily.ddl): 
* [v_report_payment_plugin_failure_aborted_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_payment_plugin_failure_aborted_daily.ddl): 
* [v_report_payment_plugin_failure_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_payment_plugin_failure_daily.ddl): 
* [v_report_payment_success_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_payment_success_daily.ddl): 
* [v_report_payments_count.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_payments_count.ddl): 
* [v_report_payments_total_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_payments_total_daily.ddl): 
* [v_report_refunds_total_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_refunds_total_daily.ddl): 
* [v_report_revenue_recognition.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_revenue_recognition.ddl): 
* [v_report_revenue_recognition_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_revenue_recognition_daily.ddl): 
* [v_report_revenue_recognition_monthly.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_revenue_recognition_monthly.ddl): 
* [v_report_system_report_control_tag_no_test_count.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_system_report_control_tag_no_test_count.ddl): 
* [v_report_trial_starts_count_daily.ddl](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/reports/v_report_trial_starts_count_daily.ddl): 
