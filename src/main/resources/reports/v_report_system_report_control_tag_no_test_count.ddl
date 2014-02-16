create or replace view v_report_system_report_control_tag_no_test as
select
  aat.name as label
, count(distinct aat.account_id) as count 
from
  analytics_account_tags aat
where 1=1 
  and aat.name in ('OVERDUE_ENFORCEMENT_OFF', 'AUTO_PAY_OFF', 'AUTO_INVOICING_OFF', 'MANUAL_PAY', 'PARTNER')
  and report_group='default'
group by 1
;
