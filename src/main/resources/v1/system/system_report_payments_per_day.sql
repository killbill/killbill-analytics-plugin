create or replace view v_system_report_payments_per_day as
select
  tenant_record_id
, date_format(greatest(created_date, updated_date), '%Y-%m-%d') as day
, case
    when state_name IN ('AUTH_ERRORED', 'CAPTURE_ERRORED', 'CHARGEBACK_ERRORED', 'CREDIT_ERRORED', 'PURCHASE_ERRORED', 'REFUND_ERRORED', 'VOID_ERRORED') then 'ERRORED'
    when state_name IN ('AUTH_FAILED', 'CAPTURE_FAILED', 'CHARGEBACK_FAILED', 'CREDIT_FAILED', 'PURCHASE_FAILED', 'REFUND_FAILED', 'VOID_FAILED') then 'FAILED'
    when state_name IN ('AUTH_PENDING', 'CAPTURE_PENDING', 'CHARGEBACK_PENDING', 'CREDIT_PENDING', 'PURCHASE_PENDING', 'REFUND_PENDING', 'VOID_PENDING') then 'PENDING'
    when state_name IN ('AUTH_SUCCESS', 'CAPTURE_SUCCESS', 'CHARGEBACK_SUCCESS', 'CREDIT_SUCCESS', 'PURCHASE_SUCCESS', 'REFUND_SUCCESS', 'VOID_SUCCESS') then 'SUCCESS'
    else 'OTHER'
  end as payment_status
, count(*)  as count
from payments
group by 1, 2, 3
order by 1, 2, 3  asc
;
