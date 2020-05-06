create or replace view v_report_invoice_item_credits_daily as
select
  aic.tenant_record_id
, aic.currency
, date_format(aic.created_date,'%Y-%m-%d') as day
, sum(aic.amount) as count
from
  analytics_invoice_credits aic
where 1=1
  and aic.report_group='default'
group by 1,2,3
;
