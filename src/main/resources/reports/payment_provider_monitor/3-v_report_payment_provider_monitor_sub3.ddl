create or replace view v_report_payment_provider_monitor_sub3 as
SELECT
  plugin_name
, merchant_account
, payment_method
, tenant_record_id
, sum(ifnull(success_count_last_hour,0)) as success_count_last_hour
, sum(ifnull(success_count_last_12_hours,0)) as success_count_last_12_hours
FROM v_report_payment_provider_monitor_sub2 t2
GROUP BY
  plugin_name
, merchant_account
, payment_method
, tenant_record_id
;
