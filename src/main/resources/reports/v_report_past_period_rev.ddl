create or replace view v_report_past_period_rev as
select
  rr.recognized_date as recognized_date
, sum(rr.recognized_amount) as recognized_amount 
from
  recognized_revenue rr
where 1=1 
  and rr.effective_date=last_day(rr.effective_date)
  and rr.effective_date < rr.recognized_date
group by 1
;
