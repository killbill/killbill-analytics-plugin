create or replace view v_report_payment_provider_monitor_sub2 as
SELECT
  apa.plugin_name
, ifnull(apa.plugin_property_4,'unknown') as merchant_account
, ifnull(apa.plugin_property_5,'unknown') as payment_method
, apa.tenant_record_id
, sum(case when apa.created_date > sysdate() - interval 1 hour then 1 else 0 end) success_count_last_hour
, count(1) success_count_last_12_hours
FROM analytics_payment_auths apa
WHERE 1=1
AND apa.payment_transaction_status = 'SUCCESS'
AND apa.created_date > sysdate() - interval '12' hour
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
, sum(case when app.created_date > sysdate() - interval 1 hour then 1 else 0 end) success_count_last_hour
, count(1) success_count_last_12_hours
FROM analytics_payment_purchases app
WHERE 1=1
AND app.payment_transaction_status = 'SUCCESS'
AND app.created_date > sysdate() - interval '12' hour
GROUP BY
  app.plugin_name
, ifnull(app.plugin_property_4,'unknown')
, ifnull(app.plugin_property_5,'unknown')
, app.tenant_record_id
;
