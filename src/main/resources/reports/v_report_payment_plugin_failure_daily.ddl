create or replace view v_report_payment_plugin_failure_daily as
select
  date_format(updated_date, '%Y-%m-%d') as day
, count(*) as count
from
  payments
  join analytics_invoices using(invoice_id)
where 1=1
  and analytics_invoices.balance > 0
  and payment_status = 'PLUGIN_FAILURE'
group by date_format(updated_date, '%Y-%m-%d')
order by date_format(updated_date, '%Y-%m-%d') asc
;
