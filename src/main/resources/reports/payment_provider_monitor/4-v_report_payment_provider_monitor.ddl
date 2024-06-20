create or replace view v_report_payment_provider_monitor as
SELECT
  plugin_list.plugin_name
, plugin_list.merchant_account
, plugin_list.payment_method
, plugin_list.tenant_record_id
, ifnull(recent_success_trx.success_count_last_hour,0) as success_count_last_hour
, ifnull(recent_success_trx.success_count_last_12_hours,0) as success_count_last_12_hours
, sysdate() as refresh_date
FROM v_report_payment_provider_monitor_sub1 plugin_list
LEFT OUTER JOIN v_report_payment_provider_monitor_sub3 recent_success_trx on
    plugin_list.plugin_name=recent_success_trx.plugin_name
AND plugin_list.merchant_account=recent_success_trx.merchant_account
AND plugin_list.payment_method=recent_success_trx.payment_method
AND plugin_list.tenant_record_id=recent_success_trx.tenant_record_id
;
