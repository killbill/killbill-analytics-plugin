create or replace view v_report_payment_provider_monitor_sub1 as
SELECT distinct
  apa.plugin_name
, ifnull(apa.plugin_property_4,'unknown') as merchant_account
, ifnull(apa.plugin_property_5,'unknown') as payment_method
, apa.tenant_record_id
FROM analytics_payment_auths apa
WHERE 1=1
AND apa.created_date > now() - interval '7' day
UNION
SELECT distinct
  app.plugin_name
, ifnull(app.plugin_property_4,'unknown') as merchant_account
, ifnull(app.plugin_property_5,'unknown') as payment_method
, app.tenant_record_id
FROM analytics_payment_purchases app
WHERE 1=1
AND app.created_date > now() - interval '7' day
;
