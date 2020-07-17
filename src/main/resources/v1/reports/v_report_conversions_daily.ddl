create or replace view v_report_conversions_daily as
select
  ast.tenant_record_id
, date_format(ast.next_start_date,'%Y-%m-%d') as day
, count(0) as count
from
  analytics_subscription_transitions ast
where 1=1
  and ast.prev_phase='TRIAL'
  and ast.next_phase!='TRIAL'
  and ast.report_group='default'
group by 1,2
;
