create or replace view v_report_invoices_balance_daily as
select
  ai.currency 
, date_format(ai.created_date,'%Y-%m-%d') as day
, sum(ai.converted_balance) as count 
from
  analytics_invoices ai
where 1=1 
  and ai.report_group='default' 
group by 1,2 
order by 1,2
;

