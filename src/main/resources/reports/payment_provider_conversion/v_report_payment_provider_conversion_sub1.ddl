create or replace view v_report_payment_provider_conversion_sub1 as
SELECT
  apa.plugin_name
, ifnull(apa.plugin_property_4,'unknown') as merchant_account
, ifnull(apa.plugin_property_5,'unknown') as payment_method
, apa.tenant_record_id
, sum(case when apa.payment_transaction_status='SUCCESS' then 1 else 0 end) as current_success_count
, count(1) as current_transaction_count
, count(distinct apa.account_id) as current_customer_count
FROM
    analytics_payment_auths apa
WHERE 1=1
AND apa.created_date >= FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60))
AND cast(FROM_UNIXTIME(UNIX_TIMESTAMP(apa.created_date) - UNIX_TIMESTAMP(apa.created_date)%(15*60)) as time) = cast(FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60)) as time)
AND apa.created_date >= sysdate() - interval '14' day
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
, sum(case when app.payment_transaction_status='SUCCESS' then 1 else 0 end) as current_success_count
, count(1) as current_transaction_count
, count(distinct app.account_id) as current_customer_count
FROM
    analytics_payment_purchases app
WHERE 1=1
AND app.created_date >= FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60))
AND cast(FROM_UNIXTIME(UNIX_TIMESTAMP(app.created_date) - UNIX_TIMESTAMP(app.created_date)%(15*60)) as time) = cast(FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60)) as time)
AND app.created_date >= sysdate() - interval '14' day
GROUP BY
  app.plugin_name
, ifnull(app.plugin_property_4,'unknown')
, ifnull(app.plugin_property_5,'unknown')
, app.tenant_record_id
;
