create or replace view v_report_trial_starts_count_daily as
select
  ast.next_start_date as day
, ast.next_product_name as product
, count(1) as count 
from
  analytics_subscription_transitions ast
where 1=1 
  and ast.report_group='default'
  and ast.prev_phase is null
  and ast.next_phase='TRIAL'
  and ast.next_start_date
  and ast.event='START_ENTITLEMENT_BASE'
group by ast.next_start_date, ast.next_product_name
;

