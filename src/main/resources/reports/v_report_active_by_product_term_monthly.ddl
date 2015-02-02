create or replace view v_report_active_by_product_term_monthly as
select
  tenant_record_id
, cal.d day
, next_product_name product_name
, next_billing_period billing_period
, count(1) count
from
  analytics_subscription_transitions ast
  join calendar cal on next_start_date < cal.d and (next_end_date > cal.d or next_end_date is null ) and (cal.d = last_day(cal.d) or cal.d = date_format(sysdate(), '%Y-%m-%d'))
where 1=1
  and event in ('START_ENTITLEMENT_BASE','CHANGE_BASE','SYSTEM_CHANGE_BASE')
  and next_service = 'entitlement-service'
  and cal.d < sysdate()
  and next_mrr > 0
  and report_group = 'default'
group by 1,2,3,4
;
