create or replace view v_report_refunds_total_daily as
select
  ar.currency as currency
, date_format(ar.created_date,'%Y-%m-%d') as day
, -sum(ar.converted_amount) as count
from
  analytics_payment_refunds ar
where 1=1
  and ar.report_group='default'
group by 1,2
order by 1,2
;
