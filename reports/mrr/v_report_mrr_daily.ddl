create or replace view v_report_mrr_daily as
select
  ast.tenant_record_id
, ifnull(ast.next_product_name, ast.prev_product_name) as product
, date_format(cal.d,'%Y-%m-%d') as day
, sum(ast.converted_next_mrr) as count
from
  calendar cal
  left join analytics_subscription_transitions ast on cast(date_format(ast.next_start_date, '%Y-%m-%d') as date) <= cast(date_format(cal.d, '%Y-%m-%d') as date)
    and case when ast.next_end_date is not null then ast.next_end_date > cast(date_format(cal.d, '%Y-%m-%d') as date) else 1=1 end
where 1=1
  and cal.d <= now()
  and ast.report_group='default'
  and ast.next_service='entitlement-service'
  and ifnull(ast.next_mrr, 0) > 0
group by 1,2,3
union select
  ast.tenant_record_id
, 'ALL' as product
, date_format(cal.d,'%Y-%m-%d') as day
, sum(ast.converted_next_mrr) as count
from
  calendar cal
  left join analytics_subscription_transitions ast on cast(date_format(ast.next_start_date, '%Y-%m-%d') as date) <= cast(date_format(cal.d, '%Y-%m-%d') as date)
    and case when ast.next_end_date is not null then ast.next_end_date > cast(date_format(cal.d, '%Y-%m-%d') as date) else 1=1 end
where 1=1
  and cal.d <= now()
  and ast.report_group='default'
  and ast.next_service='entitlement-service'
  and ifnull(ast.next_mrr, 0) > 0
group by 1,2,3
;
