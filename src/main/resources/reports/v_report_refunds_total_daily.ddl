create or replace view v_report_refunds_total_daily as
select
  ar.tenant_record_id
, ar.currency as currency
, date_format(ar.created_date,'%Y-%m-%d') as day
, -sum(ar.converted_amount) as count
from
  analytics_payment_refunds ar
where 1=1
  and ar.report_group='default'
group by 1,2,3
;
