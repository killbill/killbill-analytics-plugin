create or replace view v_report_payment_failure_aborted_daily as
select
  date_format(updated_date, '%Y-%m-%d') as day
, count(*) as count
from
  payments
  join analytics_invoices using (invoice_id)
where 1=1
  and analytics_invoices.balance > 0
  and payment_status = 'PAYMENT_FAILURE_ABORTED'
group by date_format(updated_date, '%Y-%m-%d')
order by date_format(updated_date, '%Y-%m-%d') asc
;
