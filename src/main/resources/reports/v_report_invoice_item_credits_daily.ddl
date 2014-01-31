create or replace view v_report_invoice_credits_daily as
select
  aic.currency
, date_format(aic.created_date,'%Y-%m-%d') as day
, sum(aic.amount) as count 
from
  analytics_invoice_credits aic 
where 1=1 
  and aic.report_group='default' 
group by 1,2 
order by 1,2
;
