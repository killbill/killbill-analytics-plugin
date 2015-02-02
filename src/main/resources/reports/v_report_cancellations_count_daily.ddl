create or replace view v_report_cancellations_count_daily as
select
  ast.tenant_record_id
, ast.prev_phase phase
, date_format(ast.next_start_date,'%Y-%m-%d') as day
, count(0) as count
from
  analytics_subscription_transitions ast
where 1=1
  and ast.event='STOP_ENTITLEMENT_BASE'
  and ast.report_group='default'
  and ast.prev_phase is not null
group by 1,2,3
;
