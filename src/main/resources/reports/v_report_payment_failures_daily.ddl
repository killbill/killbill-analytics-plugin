create or replace view v_report_payment_failures_daily as
select
  date_format(p.created_date,'%Y-%m-%d') as day
, case when p.payment_status='SUCCESS' then 'SUCCESS' when p.payment_status in ('PAYMENT_FAILURE', 'PAYMENT_FAILURE_ABORTED') then 'PAYMENT_FAILURE_OR_ABORTED' else 'OTHER' end as payment_status
, count(0) as count 
from
  payments p 
  join invoices i on p.invoice_id=i.id
where 1=1 
  and i.created_by!='stephaneb@glam.com'
group by 1,2 
order by 1,2
;
