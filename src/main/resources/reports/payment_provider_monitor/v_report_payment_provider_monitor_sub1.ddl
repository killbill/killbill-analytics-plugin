create or replace view v_report_payment_provider_monitor_sub1 as
SELECT distinct
  apc.plugin_name
, apc.tenant_record_id
FROM analytics_payment_captures apc
WHERE 1=1
AND apc.created_date > sysdate() - interval '7' day
UNION
SELECT distinct
  app.plugin_name
, app.tenant_record_id
FROM analytics_payment_purchases app
WHERE 1=1
AND app.created_date > sysdate() - interval '7' day
;
