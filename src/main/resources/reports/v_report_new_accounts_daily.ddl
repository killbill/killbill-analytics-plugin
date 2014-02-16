create or replace view v_report_new_accounts_daily as
select
  date_format(aa.created_date,'%Y-%m-%d') as day
, count(0) as count 
from
  analytics_accounts aa
where 1=1 
  and aa.report_group='default' 
group by 1 
order by 1
;
