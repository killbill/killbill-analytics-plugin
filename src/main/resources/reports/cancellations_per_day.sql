create or replace view v_cancellations_per_day as
select
  prev_phase as pivot
, date_format(next_start_date, '%Y-%m-%d') as day
, count(*) as count
from analytics_subscription_transitions
where 1 = 1
and event = 'STOP_ENTITLEMENT_BASE'
and report_group = 'default'
and prev_phase is not NULL
group by 1, 2
order by 1, 2 asc
;
