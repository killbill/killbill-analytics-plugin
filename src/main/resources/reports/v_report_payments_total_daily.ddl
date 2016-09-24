create or replace view v_report_payments_total_daily as
select
  ac.tenant_record_id
, 'CAPTURE' as op
, ac.currency
, date_format(ac.created_date,'%Y-%m-%d') as day
, sum(ac.converted_amount) as count
from analytics_payment_captures ac
where 1=1
  and ac.report_group='default'
group by 1,2,3,4
union
select
  ap.tenant_record_id
, 'PURCHASE' as op
, ap.currency
, date_format(ap.created_date,'%Y-%m-%d') as day
, sum(ap.converted_amount) as count
from analytics_payment_purchases ap
where 1=1
  and ap.report_group='default'
group by 1,2,3,4
;
