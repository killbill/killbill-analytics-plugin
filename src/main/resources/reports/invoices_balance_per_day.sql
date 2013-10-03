create or replace view v_invoices_balance_per_day as
select
  currency
, date_format(created_date, '%Y-%m-%d') as day
, sum(balance) as count
from analytics_invoices
where report_group = 'default'
group by 1, 2
order by 1, 2 asc
;

