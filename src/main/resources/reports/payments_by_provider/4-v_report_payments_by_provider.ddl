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
, sum(ifnull(total,0)) as total
, sum(ifnull(failed,0)) as failed
, sum(ifnull(pending,0)) as pending
, sum(ifnull(good,0)) as good
, case when failed is not null and total is not null then concat(round(((sum(failed)/sum(total))*100),2),'%')
       else '0%'
  end as pct_failed
, case when failed is not null and total is not null then concat(round(((sum(pending)/sum(total))*100),2),'%')
       else '0%'
  end as pct_pending
, case when failed is not null and total is not null then concat(round(((sum(good)/sum(total))*100),2),'%')
       else '0%'
  end as pct_good
, converted_amount
, t1.converted_currency
, sysdate() as refresh_date
FROM v_report_payments_by_provider_sub2 t1
INNER JOIN v_report_payments_by_provider_sub3 t2
LEFT OUTER JOIN v_report_payments_by_provider_sub1 v1 on v1.plugin_name=t1.plugin_name
AND v1.merchant_account=t1.merchant_account
AND v1.payment_method=t1.payment_method
AND v1.timeframe=t2.timeframe
AND v1.converted_currency=t1.converted_currency
AND v1.tenant_record_id=t1.tenant_record_id
GROUP BY
  plugin_name
, merchant_account
, payment_method
, timeframe
, transaction_type
, converted_currency
, tenant_record_id
ORDER BY
  tenant_record_id
, merchant_account
, payment_method
, plugin_name
, timeframe
, transaction_type
, converted_currency
;
