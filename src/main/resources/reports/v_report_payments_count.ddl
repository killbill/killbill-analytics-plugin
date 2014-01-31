create or replace view v_report_payments_count as
select
  payments.payment_status as label
, count(0) as count 
from
  payments 
group by 1
;
