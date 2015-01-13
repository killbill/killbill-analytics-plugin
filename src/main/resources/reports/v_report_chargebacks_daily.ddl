create or replace view v_report_chargebacks_daily as
select
  ac.currency
, date_format(ac.created_date,'%Y-%m-%d') as day
, sum(ac.converted_amount) as count
from
  analytics_payment_chargebacks ac
where 1=1
  and ac.report_group='default'
group by 1,2
order by 1,2
;
