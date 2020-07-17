create or replace view v_report_payment_provider_errors_sub2 as
select
  v1.tenant_record_id
, v1.day
, v1.currency
, v1.plugin_name
, substring_index(ifnull(apa.plugin_gateway_error, app.plugin_gateway_error), ' ', 10) as plugin_gateway_error
, count(1) as count
from v_report_payment_provider_errors_sub1 v1
left join analytics_payment_auths apa on apa.record_id = v1.record_id and v1.op = 'AUTH'
left join analytics_payment_purchases app on app.record_id = v1.record_id and v1.op = 'PURCHASE'
where 1=1
and ifnull(apa.plugin_gateway_error, app.plugin_gateway_error) is not null
group by 1,2,3,4,5
;
