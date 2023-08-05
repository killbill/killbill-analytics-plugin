create or replace view v_report_payments_total_daily as
select
  tenant_record_id
, day
, currency
, sum(count) as count
from v_report_payments_total_daily_sub1
group by 1,2,3
;
