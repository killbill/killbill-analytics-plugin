create or replace view v_report_payment_provider_monitor_sub1 as
SELECT distinct
  apa.plugin_name
, ifnull(apa.plugin_property_4,'unknown') as merchant_account
, ifnull(apa.plugin_property_5,'unknown') as payment_method
, apa.tenant_record_id
FROM analytics_payment_auths apa
WHERE 1=1
AND apa.created_date > utc_timestamp() - interval '7' day
UNION
SELECT distinct
  app.plugin_name
, ifnull(app.plugin_property_4,'unknown') as merchant_account
, ifnull(app.plugin_property_5,'unknown') as payment_method
, app.tenant_record_id
FROM analytics_payment_purchases app
WHERE 1=1
AND app.created_date > utc_timestamp() - interval '7' day
;

create or replace view v_report_payment_provider_monitor_sub2 as
SELECT
  apa.plugin_name
, ifnull(apa.plugin_property_4,'unknown') as merchant_account
, ifnull(apa.plugin_property_5,'unknown') as payment_method
, apa.tenant_record_id
, sum(case when apa.created_date > utc_timestamp() - interval 1 hour then 1 else 0 end) success_count_last_hour
, count(1) success_count_last_12_hours
FROM analytics_payment_auths apa
WHERE 1=1
AND apa.payment_transaction_status = 'SUCCESS'
AND apa.created_date > utc_timestamp() - interval '12' hour
GROUP BY
  apa.plugin_name
, ifnull(apa.plugin_property_4,'unknown')
, ifnull(apa.plugin_property_5,'unknown')
, apa.tenant_record_id
UNION
SELECT
  app.plugin_name
, ifnull(app.plugin_property_4,'unknown') as merchant_account
, ifnull(app.plugin_property_5,'unknown') as payment_method
, app.tenant_record_id
, sum(case when app.created_date > utc_timestamp() - interval 1 hour then 1 else 0 end) success_count_last_hour
, count(1) success_count_last_12_hours
FROM analytics_payment_purchases app
WHERE 1=1
AND app.payment_transaction_status = 'SUCCESS'
AND app.created_date > utc_timestamp() - interval '12' hour
GROUP BY
  app.plugin_name
, ifnull(app.plugin_property_4,'unknown')
, ifnull(app.plugin_property_5,'unknown')
, app.tenant_record_id
;

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