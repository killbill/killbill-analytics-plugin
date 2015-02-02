create or replace view v_report_payments_total_daily as
select
  ap.tenant_record_id
, ap.currency
, date_format(ap.created_date,'%Y-%m-%d') as day
, sum(ap.converted_amount) as count
-- TODO add analytics_payment_captures?
from analytics_payment_purchases ap
where 1=1
  and ap.report_group='default'
group by 1,2,3
;
