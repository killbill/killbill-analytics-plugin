create or replace view v_report_payment_provider_conversion as
select
     rpccs1.plugin_name
    ,ifnull(sum(rpccs1.current_success_count),0) as current_success_count
    ,ifnull(sum(rpccs1.current_transaction_count),0) as current_transaction_count
    ,ifnull(sum(rpccs1.current_customer_count),0) as current_customer_count
    ,sum(rpccs2.historical_success_count) as historical_success_count
    ,sum(rpccs2.historical_transaction_count) as historical_transaction_count    
    ,'1970-01-01' as day
    ,1 as count
    ,rpccs1.tenant_record_id    
from
    v_report_payment_provider_conversion_sub2 rpccs2
    LEFT OUTER JOIN v_report_payment_provider_conversion_sub1 rpccs1 ON
        rpccs1.plugin_name=rpccs2.plugin_name
        AND rpccs1.tenant_record_id=rpccs2.tenant_record_id
GROUP BY
    plugin_name
    ,tenant_record_id
;