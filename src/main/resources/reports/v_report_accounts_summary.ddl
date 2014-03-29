create or replace view v_report_accounts_summary as
select
  case when nb_active_bundles <= 0 then 'Non-subscriber' else 'Subscriber' end as label
, count(distinct a.account_record_id) as count
from
    analytics_accounts a
where 1 = 1
  and report_group = 'default'
group by 1
;
