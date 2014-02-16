create or replace view v_report_payments_total_daily as
select
  ap.currency 
, date_format(ap.created_date,'%Y-%m-%d') as day
, sum(ap.converted_amount) as count 
from
  analytics_payments ap 
where 1=1 
  and ap.report_group='default' 
group by ap.currency, date_format(ap.created_date, '%Y-%m-%d') 
order by ap.currency, date_format(ap.created_date, '%Y-%m-%d')
;
