create or replace view v_report_payment_provider_conversion as
select
  rpccs1.plugin_name
, rpccs1.merchant_account
, rpccs1.payment_method
, rpccs1.tenant_record_id    
, ifnull(sum(rpccs1.current_success_count),0) as current_success_count
, ifnull(sum(rpccs1.current_transaction_count),0) as current_transaction_count
, ifnull(sum(rpccs1.current_customer_count),0) as current_customer_count
, ifnull(sum(rpccs2.historical_success_count),0) as historical_success_count
, ifnull(sum(rpccs2.historical_transaction_count),0) as historical_transaction_count
, ifnull(sum(rpccs2.historical_customer_count),0) as historical_customer_count
, case when current_success_count is not null and historical_success_count is not null
       then concat(round((((sum(rpccs1.current_success_count)-sum(rpccs2.historical_success_count))/sum(rpccs2.historical_success_count))*100),2),'%')
       else '0%'
  end success_delta
, case when current_transaction_count is not null and historical_transaction_count is not null
       then concat(round((((sum(rpccs1.current_transaction_count)-sum(rpccs2.historical_transaction_count))/sum(rpccs2.historical_transaction_count))*100),2),'%')
       else '0%'
  end transaction_delta
, case when current_customer_count is not null and historical_customer_count is not null
       then concat(round((((sum(rpccs1.current_customer_count)-sum(rpccs2.historical_customer_count))/sum(rpccs2.historical_customer_count))*100),2),'%')
       else '0%'
  end customer_delta
, date_format(sysdate(), '%Y-%m-%d') as day
, 1 as count
from v_report_payment_provider_conversion_sub2 rpccs2
LEFT OUTER JOIN v_report_payment_provider_conversion_sub1 rpccs1 ON
    rpccs1.plugin_name=rpccs2.plugin_name
AND rpccs1.merchant_account=rpccs2.merchant_account
AND rpccs1.payment_method=rpccs2.payment_method
AND rpccs1.tenant_record_id=rpccs2.tenant_record_id
GROUP BY
  plugin_name
, merchant_account
, payment_method
, tenant_record_id
;
