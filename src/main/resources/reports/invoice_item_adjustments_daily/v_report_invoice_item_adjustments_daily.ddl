create or replace view v_report_invoice_item_adjustments_daily as
select
  aiia.tenant_record_id
, aiia.currency
, date_format(aiia.created_date,'%Y-%m-%d') as day
, sum(aiia.converted_amount) as count
from
  analytics_invoice_item_adjustments aiia
where 1=1
  and aiia.report_group='default'
group by 1,2,3
;
