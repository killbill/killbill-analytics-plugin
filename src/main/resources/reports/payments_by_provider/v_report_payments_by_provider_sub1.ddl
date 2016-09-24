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
FROM analytics_payment_auths a
FORCE INDEX(analytics_payment_auths_created_date)
WHERE 1=1
AND a.created_date>now() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_captures a
FORCE INDEX(analytics_payment_captures_created_date)
WHERE 1=1
AND a.created_date>now() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_chargebacks a
FORCE INDEX(analytics_payment_chargebacks_created_date)
WHERE 1=1
AND a.created_date>now() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_credits a
FORCE INDEX(analytics_payment_credits_created_date)
WHERE 1=1
AND a.created_date>now() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_purchases a
FORCE INDEX(analytics_payment_purchases_created_date)
WHERE 1=1
AND a.created_date>now() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_refunds a
FORCE INDEX(analytics_payment_refunds_created_date)
WHERE 1=1
AND a.created_date>now() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_voids a
FORCE INDEX(analytics_payment_voids_created_date)
WHERE 1=1
AND a.created_date>now() - interval '7' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_auths a
WHERE 1=1
AND a.created_date>now() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_captures a
WHERE 1=1
AND a.created_date>now() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_chargebacks a
WHERE 1=1
AND a.created_date>now() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_credits a
WHERE 1=1
AND a.created_date>now() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_purchases a
WHERE 1=1
AND a.created_date>now() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_refunds a
WHERE 1=1
AND a.created_date>now() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_voids a
WHERE 1=1
AND a.created_date>now() - interval '1' day
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_auths a
WHERE 1=1
AND a.created_date>now() - interval '34' minute
AND a.created_date<=now() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_captures a
WHERE 1=1
AND a.created_date>now() - interval '34' minute
AND a.created_date<=now() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_chargebacks a
WHERE 1=1
AND a.created_date>now() - interval '34' minute
AND a.created_date<=now() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_credits a
WHERE 1=1
AND a.created_date>now() - interval '34' minute
AND a.created_date<=now() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_purchases a
WHERE 1=1
AND a.created_date>now() - interval '34' minute
AND a.created_date<=now() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_refunds a
WHERE 1=1
AND a.created_date>now() - interval '34' minute
AND a.created_date<=now() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
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
FROM analytics_payment_voids a
WHERE 1=1
AND a.created_date>now() - interval '34' minute
AND a.created_date<=now() - interval '4' minute
GROUP BY
  a.plugin_name
, ifnull(a.plugin_property_4,'unknown')
, ifnull(a.plugin_property_5,'unknown')
, a.tenant_record_id
;
