create or replace view v_report_product_term_monthly as
select
  cal.d as day
, ast.next_product_name as product_name
, ast.next_billing_period as billing_period
, count(1) as count 
from
  analytics_subscription_transitions ast join calendar cal on ast.next_start_date < cal.d
  and ast.next_end_date > cal.d or ast.next_end_date is null
  and cal.d=last_day(cal.d) or cal.d=date_format(sysdate(), '%Y-%m-%d')
where 1=1 
  and ast.event in ('START_ENTITLEMENT_BASE', 'CHANGE_BASE', 'SYSTEM_CHANGE_BASE', 'RESUME_ENTITLEMENT_BASE')
  and ast.next_service='entitlement-service'
  and ast.next_product_category='BASE'
  and cal.d < sysdate()
  and ast.next_mrr > 0
  and ast.report_group='default'
group by cal.d, ast.next_product_name, ast.next_billing_period
;
