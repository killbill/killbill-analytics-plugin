create or replace view v_mrr_per_day as
select
  ifnull(next_product_name, prev_product_name) as product
, date_format(calendar.d, '%Y-%m-%d') as day
, sum(ast.converted_next_mrr) as count
from calendar
left join analytics_subscription_transitions ast on date_format(ast.next_start_date, '%Y-%m-%d') <= date_format(calendar.d, '%Y-%m-%d')
                                                and (case when ast.next_end_date is not null then ast.next_end_date > date_format(calendar.d, '%Y-%m-%d') else 1 = 1 end)
where 1 = 1
and calendar.d >= '2013-01-01'
and calendar.d <= now()
and ast.report_group = 'default'
and ast.next_service = 'entitlement-service'
and ifnull(ast.next_mrr, 0) > 0
group by 1, 2

union

select
  'ALL' as product
, date_format(calendar.d, '%Y-%m-%d') as day
, sum(ast.converted_next_mrr) as count
from calendar
left join analytics_subscription_transitions ast on date_format(ast.next_start_date, '%Y-%m-%d') <= date_format(calendar.d, '%Y-%m-%d')
                                                and (case when ast.next_end_date is not null then ast.next_end_date > date_format(calendar.d, '%Y-%m-%d') else 1 = 1 end)
where 1 = 1
and calendar.d >= '2013-01-01'
and calendar.d <= now()
and ast.report_group = 'default'
and ast.next_service = 'entitlement-service'
and ifnull(ast.next_mrr, 0) > 0
group by 1, 2
;
