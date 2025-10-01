create or replace view v_report_overdue_accounts_summary AS
select a.tenant_record_id AS tenant_record_id,
(case when (a.balance <= 0) then 'Current' else 'Overdue' end) AS label,
count(0) AS count
from analytics_accounts a
where
((1 = 1) and
(a.report_group = 'default'))
group by a.tenant_record_id,
(case when (a.balance <= 0) then 'Current' else 'Overdue' end)