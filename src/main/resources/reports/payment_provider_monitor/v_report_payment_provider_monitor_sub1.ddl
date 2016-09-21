create or replace view v_report_payment_provider_monitor_sub1 as
SELECT distinct
  apc.plugin_name
, ifnull(apc.plugin_property_4,'unknown') as merchant_account
, ifnull(apc.plugin_property_5,'unknown') as payment_method
, apc.tenant_record_id
FROM analytics_payment_auths apc
WHERE 1=1
AND apc.created_date > sysdate() - interval '7' day
UNION
SELECT distinct
  app.plugin_name
, ifnull(app.plugin_property_4,'unknown') as merchant_account
, ifnull(app.plugin_property_5,'unknown') as payment_method
, app.tenant_record_id
FROM analytics_payment_purchases app
WHERE 1=1
AND app.created_date > sysdate() - interval '7' day
;
