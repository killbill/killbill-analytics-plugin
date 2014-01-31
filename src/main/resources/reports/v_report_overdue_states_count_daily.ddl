create or replace view v_report_overdue_states_count_daily as
select
  aat.state
, date_format(cal.d,'%Y-%m-%d') as day
, count(1) as count 
from
  calendar cal
  join analytics_account_transitions aat on date_format(aat.start_date, '%Y-%m-%d')=date_format(cal.d, '%Y-%m-%d')
where 1=1 
  and aat.report_group='default'
  and aat.service='overdue-service'
  and cal.d >= '2013-01-01'
  and cal.d <= now()
group by 1,2 
order by 1,2
;
