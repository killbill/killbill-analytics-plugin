create or replace view v_system_report_notifications_per_queue_name as
select
  search_key2 as tenant_record_id
, queue_name
, date_format(effective_date, '%Y-%m-%d') as day
, count(*) as count
from notifications
where processing_state = 'AVAILABLE'
group by 1, 2, 3
order by 1, 2, 3 asc
;
