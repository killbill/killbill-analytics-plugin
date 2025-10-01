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
AND apa.created_date >= FROM_UNIXTIME(UNIX_TIMESTAMP(UTC_TIMESTAMP()) - 15*60 - (UNIX_TIMESTAMP(UTC_TIMESTAMP()) - 15*60)%(15*60))
AND cast(FROM_UNIXTIME(UNIX_TIMESTAMP(apa.created_date) - UNIX_TIMESTAMP(apa.created_date)%(15*60)) as time) = cast(FROM_UNIXTIME(UNIX_TIMESTAMP(UTC_TIMESTAMP()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60)) as time)
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
AND app.created_date >= FROM_UNIXTIME(UNIX_TIMESTAMP(UTC_TIMESTAMP()) - 15*60 - (UNIX_TIMESTAMP(UTC_TIMESTAMP()) - 15*60)%(15*60))
AND cast(FROM_UNIXTIME(UNIX_TIMESTAMP(app.created_date) - UNIX_TIMESTAMP(app.created_date)%(15*60)) as time) = cast(FROM_UNIXTIME(UNIX_TIMESTAMP(UTC_TIMESTAMP()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60)) as time)
GROUP BY
  app.plugin_name
, ifnull(app.plugin_property_4,'unknown')
, ifnull(app.plugin_property_5,'unknown')
, app.tenant_record_id
;

create or replace view v_report_payment_provider_conversion_sub2 as
SELECT
  apa.plugin_name
, ifnull(apa.plugin_property_4,'unknown') as merchant_account
, ifnull(apa.plugin_property_5,'unknown') as payment_method
, apa.tenant_record_id
, sum(case when apa.payment_transaction_status='SUCCESS' then 1 else 0 end) as historical_success_count
, count(1) as historical_transaction_count
, count(distinct apa.account_id) as historical_customer_count
FROM
    analytics_payment_auths apa
WHERE 1=1
AND apa.created_date < FROM_UNIXTIME(UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60 - (UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60)%(15*60) + 15*60)
AND cast(FROM_UNIXTIME(UNIX_TIMESTAMP(apa.created_date) - UNIX_TIMESTAMP(apa.created_date)%(15*60)) as time) = cast(FROM_UNIXTIME(UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60 - (UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60)%(15*60)) as time)
AND apa.created_date >= FROM_UNIXTIME(UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60 - (UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60)%(15*60))
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
, sum(case when app.payment_transaction_status='SUCCESS' then 1 else 0 end) as historical_success_count
, count(1) as historical_transaction_count
, count(distinct app.account_id) as historical_customer_count
FROM
    analytics_payment_purchases app
WHERE 1=1
AND app.created_date < FROM_UNIXTIME(UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60 - (UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60)%(15*60) + 15*60)
AND cast(FROM_UNIXTIME(UNIX_TIMESTAMP(app.created_date) - UNIX_TIMESTAMP(app.created_date)%(15*60)) as time) = cast(FROM_UNIXTIME(UNIX_TIMESTAMP(NOW() - interval '14' day) - 15*60 - (UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60)%(15*60)) as time)
AND app.created_date >= FROM_UNIXTIME(UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60 - (UNIX_TIMESTAMP(UTC_TIMESTAMP() - interval '14' day) - 15*60)%(15*60))
GROUP BY
  app.plugin_name
, ifnull(app.plugin_property_4,'unknown')
, ifnull(app.plugin_property_5,'unknown')
, app.tenant_record_id
;

create or replace view v_report_payment_provider_conversion as
with agg as (
  select
    rpccs1.plugin_name,
    rpccs1.merchant_account,
    rpccs1.payment_method,
    rpccs1.tenant_record_id,
    ifnull(sum(rpccs1.current_success_count),0) as current_success_count,
    ifnull(sum(rpccs1.current_transaction_count),0) as current_transaction_count,
    ifnull(sum(rpccs1.current_customer_count),0) as current_customer_count,
    ifnull(sum(rpccs2.historical_success_count),0) as historical_success_count,
    ifnull(sum(rpccs2.historical_transaction_count),0) as historical_transaction_count,
    ifnull(sum(rpccs2.historical_customer_count),0) as historical_customer_count
  from v_report_payment_provider_conversion_sub2 rpccs2
  left join v_report_payment_provider_conversion_sub1 rpccs1
    on rpccs1.plugin_name=rpccs2.plugin_name
   and rpccs1.merchant_account=rpccs2.merchant_account
   and rpccs1.payment_method=rpccs2.payment_method
   and rpccs1.tenant_record_id=rpccs2.tenant_record_id
  group by
    rpccs1.plugin_name,
    rpccs1.merchant_account,
    rpccs1.payment_method,
    rpccs1.tenant_record_id
)
select *,
  case when historical_success_count > 0
       then concat(round(((current_success_count-historical_success_count)/historical_success_count)*100,2),'%')
       else '0%'
  end as success_delta,
  case when historical_transaction_count > 0
       then concat(round(((current_transaction_count-historical_transaction_count)/historical_transaction_count)*100,2),'%')
       else '0%'
  end as transaction_delta,
  case when historical_customer_count > 0
       then concat(round(((current_customer_count-historical_customer_count)/historical_customer_count)*100,2),'%')
       else '0%'
  end as customer_delta,
  sysdate() as refresh_date
from agg;
