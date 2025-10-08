create or replace view v_report_payment_provider_errors_sub1 as
select
  aa.tenant_record_id
, 'AUTH' as op
, date_format(aa.created_date,'%Y-%m-%d') as day
, aa.currency
, aa.plugin_name
, aa.record_id
from analytics_payment_auths aa
where 1=1
  and aa.payment_transaction_status not in ('PENDING', 'SUCCESS')
  and aa.report_group = 'default'
  and aa.created_date > utc_timestamp() - interval '60' day
union
select
  ap.tenant_record_id
, 'PURCHASE' as op
, date_format(ap.created_date,'%Y-%m-%d') as day
, ap.currency
, ap.plugin_name
, ap.record_id
from analytics_payment_purchases ap
where 1=1
  and ap.payment_transaction_status not in ('PENDING', 'SUCCESS')
  and ap.report_group = 'default'
  and ap.created_date > utc_timestamp() - interval '60' day
;

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

