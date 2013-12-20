create or replace view v_system_report_payments_per_day as
select
date_format(greatest(effective_date, updated_date), '%Y-%m-%d') as day
, case
    when payment_status = 'SUCCESS' then 'SUCCESS'
    when payment_status IN ('PAYMENT_FAILURE', 'PAYMENT_FAILURE_ABORTED') then 'PAYMENT_FAILURE_OR_ABORTED'
    else 'OTHER'
  end as payment_status
, count(*)  as count
from
payments
group by 1, 2
order by 1, 2  asc
;
