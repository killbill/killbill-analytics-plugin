create or replace view v_report_revenue_recognition_monthly as
select
  cur_period_rev.effective_date as day
, cur_period_rev.recognized_amount + past_period_rev.recognized_amount as count 
from
  calendar cal
  join recognized_revenue cur_period_rev on cur_period_rev.recognized_date=date_format(cal.d, '%Y-%m-01') and cur_period_rev.effective_date=cal.d and (cal.d=last_day(cal.d) or cal.d>date_sub(sysdate(),interval 1 day)) and cal.d>'2013-01-01'
  join v_report_past_period_rev past_period_rev on past_period_rev.recognized_date=date_format(cal.d, '%Y-%m-01')
order by cur_period_rev.effective_date
;

