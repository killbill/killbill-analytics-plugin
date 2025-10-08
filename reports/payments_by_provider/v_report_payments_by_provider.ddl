create or replace view v_report_payments_by_provider_sub1 as
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 2 as timeframe
, a.tenant_record_id
, 'AUTHORIZE' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_auths a
FORCE INDEX(analytics_payment_auths_created_date)
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 2 as timeframe
, a.tenant_record_id
, 'CAPTURE' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_captures a
FORCE INDEX(analytics_payment_captures_created_date)
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 2 as timeframe
, a.tenant_record_id
, 'CHARGEBACK' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_chargebacks a
FORCE INDEX(analytics_payment_chargebacks_created_date)
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 2 as timeframe
, a.tenant_record_id
, 'CREDIT' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_credits a
FORCE INDEX(analytics_payment_credits_created_date)
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 2 as timeframe
, a.tenant_record_id
, 'PURCHASE' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_purchases a
FORCE INDEX(analytics_payment_purchases_created_date)
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 2 as timeframe
, a.tenant_record_id
, 'REFUND' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_refunds a
FORCE INDEX(analytics_payment_refunds_created_date)
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 2 as timeframe
, a.tenant_record_id
, 'VOID' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_voids a
FORCE INDEX(analytics_payment_voids_created_date)
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
--  ****************************************************************************************************************************
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 3 as timeframe
, a.tenant_record_id
, 'AUTHORIZE' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_auths a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 3 as timeframe
, a.tenant_record_id
, 'CAPTURE' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_captures a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 3 as timeframe
, a.tenant_record_id
, 'CHARGEBACK' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_chargebacks a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 3 as timeframe
, a.tenant_record_id
, 'CREDIT' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_credits a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 3 as timeframe
, a.tenant_record_id
, 'PURCHASE' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_purchases a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 3 as timeframe
, a.tenant_record_id
, 'REFUND' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_refunds a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 3 as timeframe
, a.tenant_record_id
, 'VOID' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_voids a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
--  ****************************************************************************************************************************
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 4 as timeframe
, a.tenant_record_id
, 'AUTHORIZE' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_auths a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '34' minute
AND a.created_date<=utc_timestamp() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 4 as timeframe
, a.tenant_record_id
, 'CAPTURE' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_captures a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '34' minute
AND a.created_date<=utc_timestamp() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 4 as timeframe
, a.tenant_record_id
, 'CHARGEBACK' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_chargebacks a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '34' minute
AND a.created_date<=utc_timestamp() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 4 as timeframe
, a.tenant_record_id
, 'CREDIT' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_credits a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '34' minute
AND a.created_date<=utc_timestamp() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 4 as timeframe
, a.tenant_record_id
, 'PURCHASE' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_purchases a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '34' minute
AND a.created_date<=utc_timestamp() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 4 as timeframe
, a.tenant_record_id
, 'REFUND' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_refunds a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '34' minute
AND a.created_date<=utc_timestamp() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
UNION
SELECT
  a.plugin_name as plugin_name
, ifnull(a.plugin_property_4,'unknown') as merchant_account
, ifnull(a.plugin_property_5,'unknown') as payment_method
, 4 as timeframe
, a.tenant_record_id
, 'VOID' as transaction_type
, count(1) as total
, sum(case when a.payment_transaction_status in ('UNKNOWN','PAYMENT_FAILURE','PLUGIN_FAILURE') then 1 else 0 end) as failed
, sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
, sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
, sum(case when a.payment_transaction_status = 'SUCCESS' then a.converted_amount else 0 end) as converted_amount
, a.converted_currency
FROM analytics_payment_voids a
WHERE 1=1
AND a.created_date>utc_timestamp() - interval '34' minute
AND a.created_date<=utc_timestamp() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.converted_currency
, a.tenant_record_id
;


create or replace view v_report_payments_by_provider_sub2 as
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,converted_currency,tenant_record_id from analytics_payment_auths force index(analytics_payment_auths_date_trid_plugin_name) where created_date > utc_timestamp() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,converted_currency,tenant_record_id from analytics_payment_captures force index(analytics_payment_captures_date_trid_plugin_name) where created_date > utc_timestamp() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,converted_currency,tenant_record_id from analytics_payment_chargebacks force index(analytics_payment_chargebacks_date_trid_plugin_name) where created_date > utc_timestamp() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,converted_currency,tenant_record_id from analytics_payment_credits force index(analytics_payment_credits_date_trid_plugin_name) where created_date > utc_timestamp() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,converted_currency,tenant_record_id from analytics_payment_purchases force index(analytics_payment_purchases_date_trid_plugin_name) where created_date > utc_timestamp() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,converted_currency,tenant_record_id from analytics_payment_refunds force index(analytics_payment_refunds_date_trid_plugin_name) where created_date > utc_timestamp() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,converted_currency,tenant_record_id from analytics_payment_voids force index(analytics_payment_voids_date_trid_plugin_name) where created_date > utc_timestamp() - interval '7' day
;

create or replace view v_report_payments_by_provider_sub3 as
select  1 as timeframe union
select 2 as timeframe union
select 3 as timeframe union
select 4 as timeframe
;

create or replace view v_report_payments_by_provider as
SELECT
  t1.plugin_name
, t1.merchant_account
, t1.payment_method
, t1.tenant_record_id
, t2.timeframe
, transaction_type
, case when t2.timeframe=1 then 'Last 30 days'
       when t2.timeframe=2 then 'Last 7 days'
       when t2.timeframe=3 then 'Last 24 hours'
       when t2.timeframe=4 then 'Last 30 min'
  end as period
, sum(ifnull(total,0))   as total
, sum(ifnull(failed,0))  as failed
, sum(ifnull(pending,0)) as pending
, sum(ifnull(good,0))    as good
, case when sum(failed) is not null and sum(total) is not null
       then concat(round(((sum(failed)/sum(total))*100),2),'%')
       else '0%'
  end as pct_failed
, case when sum(pending) is not null and sum(total) is not null
       then concat(round(((sum(pending)/sum(total))*100),2),'%')
       else '0%'
  end as pct_pending
, case when sum(good) is not null and sum(total) is not null
       then concat(round(((sum(good)/sum(total))*100),2),'%')
       else '0%'
  end as pct_good
, sum(ifnull(v1.converted_amount,0)) as converted_amount
, t1.converted_currency
, CAST(sysdate() AS DATETIME) as refresh_date  -- ✅ cast to DATETIME
FROM v_report_payments_by_provider_sub2 t1
INNER JOIN v_report_payments_by_provider_sub3 t2
LEFT OUTER JOIN v_report_payments_by_provider_sub1 v1
  on v1.plugin_name        = t1.plugin_name
 and v1.merchant_account   = t1.merchant_account
 and v1.payment_method     = t1.payment_method
 and v1.timeframe          = t2.timeframe
 and v1.converted_currency = t1.converted_currency
 and v1.tenant_record_id   = t1.tenant_record_id
GROUP BY
  t1.plugin_name
, t1.merchant_account
, t1.payment_method
, t2.timeframe
, transaction_type
, t1.converted_currency
, t1.tenant_record_id
ORDER BY
  t1.tenant_record_id
, t1.merchant_account
, t1.payment_method
, t1.plugin_name
, t2.timeframe
, transaction_type
, t1.converted_currency
;


create or replace view v_report_payments_by_provider_last_24h_summary as
select
  tenant_record_id
, payment_method as label
, sum(total) as count
from v_report_payments_by_provider
where 1 = 1
and timeframe = 3
and transaction_type in ('AUTHORIZE', 'PURCHASE')
group by 1,2
;