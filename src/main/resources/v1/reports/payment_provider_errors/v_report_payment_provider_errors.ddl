create or replace view v_report_payment_provider_errors as
select
  tenant_record_id
, day
, currency
, plugin_name
, plugin_gateway_error
, count
from v_report_payment_provider_errors_sub2 sub2
where (
  select count(*) from v_report_payment_provider_errors_sub2 as sub21
  where 1=1
    and sub21.tenant_record_id = sub2.tenant_record_id
    and sub21.day = sub2.day
    and sub21.currency = sub2.currency
    and sub21.plugin_name = sub2.plugin_name
    and sub21.count >= sub2.count
) <= 3
;
