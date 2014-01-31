create or replace view v_report_notifications_per_queue_name_count_daily as
select
  notifications.queue_name as queue_name
, date_format(notifications.effective_date,'%Y-%m-%d') as day
, count(0) as count 
from
  notifications 
where 1=1 
  and notifications.processing_state='AVAILABLE'
group by 1,2 
order by 1,2
;
