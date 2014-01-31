create or replace view v_report_notifications_per_queue_name_late as
select
  notifications.queue_name as label
, count(0) as count 
from
  notifications 
where 1=1 
  and notifications.processing_state='AVAILABLE'
  and notifications.effective_date < now()
group by 1 
order by 1
;

