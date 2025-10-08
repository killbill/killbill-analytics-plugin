create or replace view v_report_new_subscriptions_daily AS
select ast.tenant_record_id AS tenant_record_id,
ast.next_slug AS slug,
date_format(ast.next_start_date,'%Y-%m-%d') AS day,
count(0) AS count
from analytics_subscription_transitions ast
where ((1 = 1) and
(ast.event = 'START_BILLING_BASE') and
(ast.report_group = 'default')) group by
ast.tenant_record_id,ast.next_slug,
date_format(ast.next_start_date,'%Y-%m-%d')