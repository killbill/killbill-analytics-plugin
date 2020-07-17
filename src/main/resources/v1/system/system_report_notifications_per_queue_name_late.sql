create or replace view v_system_report_notifications_per_queue_name_late as
select
  search_key2 as tenant_record_id
, queue_name as label
, count(*) as count
from notifications
where 1=1
and processing_state = 'AVAILABLE'
and effective_date < NOW()
-- and (processing_owner IS NULL OR processing_available_date <= NOW())
group by 1, 2
order by 1, 2 asc
;
