create or replace view v_report_payments_by_provider_last_24h_summary as
select
  tenant_record_id
, payment_method as label
, sum(total) as count
from v_report_payments_by_provider
where 1 = 1
and timeframe = 3
and transaction_type in ('AUTHORIZE', 'PURCHASE')
group by 1,2
;
