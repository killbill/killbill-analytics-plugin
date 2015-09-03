create or replace view v_report_payment_provider_conversion as 
select
     plugin_name
    ,sum(current_success_count) as current_success_count
    ,sum(current_transaction_count) as current_transaction_count
    ,sum(historical_success_count) as historical_success_count
    ,sum(historical_transaction_count) as historical_transaction_count
    ,cast('1970-01-01' as date) as day
    ,1 as count
    ,tenant_record_id
from
    v_report_payment_provider_conversion_sub1
GROUP BY
    plugin_name
    ,tenant_record_id
;
