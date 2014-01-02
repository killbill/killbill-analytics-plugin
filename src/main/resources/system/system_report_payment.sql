create or replace view v_system_report_payments as
select
payment_status as label
, count(*) as count
from payments
group by 1
;
