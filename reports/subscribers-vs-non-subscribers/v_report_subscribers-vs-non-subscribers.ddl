create or replace view v_report_subscribers_vs_non_subscribers as
select
  a.tenant_record_id
, case when nb_active_bundles <= 0 then 'Non-subscriber' else 'Subscriber' end as label
, count(distinct a.account_record_id) as count
from
  analytics_accounts a
where 1 = 1
  and report_group = 'default'
group by 1,2
;
