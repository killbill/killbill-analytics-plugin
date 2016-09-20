create or replace view v_report_payment_provider_conversion_sub2 as
SELECT
  apc.plugin_name
, apc.plugin_property_4 as merchant_account
, apc.plugin_property_5 as payment_method
, apc.tenant_record_id
, sum(case when apc.payment_transaction_status='SUCCESS' then 1 else 0 end) as historical_success_count
, count(1) as historical_transaction_count
, count(distinct apc.account_id) as historical_customer_count
FROM
    analytics_payment_auths apc
WHERE 1=1
AND apc.created_date < FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60))
AND cast(FROM_UNIXTIME(UNIX_TIMESTAMP(apc.created_date) - UNIX_TIMESTAMP(apc.created_date)%(15*60)) as time) = cast(FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60)) as time)
AND apc.created_date >= sysdate() - interval '14' day
GROUP BY
  apc.plugin_name
, apc.plugin_property_4
, apc.plugin_property_5
, apc.tenant_record_id
UNION
SELECT
  app.plugin_name
, app.plugin_property_4 as merchant_account
, app.plugin_property_5 as payment_method
, app.tenant_record_id
, sum(case when app.payment_transaction_status='SUCCESS' then 1 else 0 end) as historical_success_count
, count(1) as historical_transaction_count
, count(distinct app.account_id) as historical_customer_count
FROM
    analytics_payment_purchases app
WHERE 1=1
AND app.created_date < FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60))
AND cast(FROM_UNIXTIME(UNIX_TIMESTAMP(app.created_date) - UNIX_TIMESTAMP(app.created_date)%(15*60)) as time) = cast(FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60)) as time)
AND app.created_date >= sysdate() - interval '14' day
GROUP BY
  app.plugin_name
, app.plugin_property_4
, app.plugin_property_5
, app.tenant_record_id
;
