create or replace view v_system_report_payments as
select
payment_status
, count(*) as count
from payments
group by 1
;
