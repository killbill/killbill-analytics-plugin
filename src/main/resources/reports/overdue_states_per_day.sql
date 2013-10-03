create or replace view v_overdue_states_per_day as
select
  state
, date_format(calendar.d, '%Y-%m-%d') as day
, count(1) as count
from calendar
join analytics_account_transitions aat on date_format(aat.start_date, '%Y-%m-%d') = date_format(calendar.d, '%Y-%m-%d')
where 1 = 1
and aat.report_group = 'default'
and aat.service = 'overdue-service'
and calendar.d >= '2013-01-01'
and calendar.d <= now()
group by 1, 2
order by 1, 2 asc
;

