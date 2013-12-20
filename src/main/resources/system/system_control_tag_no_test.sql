create or replace view v_system_report_control_tag_no_test as
select
a1.name as tag_name
, count(distinct(a1.account_id)) as count
from analytics_account_tags a1
left outer join analytics_account_tags a2
on a1.account_id = a2.account_id and a2.name = 'TEST'
where 1=1
and a2.record_id IS NULL
and a1.name IN ('OVERDUE_ENFORCEMENT_OFF', 'AUTO_PAY_OFF', 'AUTO_INVOICING_OFF', 'MANUAL_PAY', 'PARTNER')
group by 1
;
