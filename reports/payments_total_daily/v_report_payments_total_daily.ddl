create or replace view v_report_payments_total_daily as
select
  tenant_record_id,
  date_format(created_date,'%Y-%m-%d') as day,
  currency,
  sum(ifnull(converted_amount, 0)) as count
from (
    select
      ac.tenant_record_id,
      ac.created_date,
      ac.currency,
      ac.converted_amount
    from analytics_payment_captures ac
    where ac.payment_transaction_status = 'SUCCESS'
      and ac.report_group='default'

    union all

    select
      ap.tenant_record_id,
      ap.created_date,
      ap.currency,
      ap.converted_amount
    from analytics_payment_purchases ap
    where ap.payment_transaction_status = 'SUCCESS'
      and ap.report_group='default'
) t
group by 1,2,3;
