create or replace view v_report_payment_provider_errors_sub1 as
select
  aa.tenant_record_id
, 'AUTH' as op
, date_format(aa.created_date,'%Y-%m-%d') as day
, aa.currency
, aa.plugin_name
, aa.record_id
from analytics_payment_auths aa
where 1=1
  and aa.payment_transaction_status not in ('PENDING', 'SUCCESS')
  and aa.report_group = 'default'
  and aa.created_date > now() - interval '60' day
union
select
  ap.tenant_record_id
, 'PURCHASE' as op
, date_format(ap.created_date,'%Y-%m-%d') as day
, ap.currency
, ap.plugin_name
, ap.record_id
from analytics_payment_purchases ap
where 1=1
  and ap.payment_transaction_status not in ('PENDING', 'SUCCESS')
  and ap.report_group = 'default'
  and ap.created_date > now() - interval '60' day
;
