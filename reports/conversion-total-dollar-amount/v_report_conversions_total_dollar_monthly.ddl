create or replace view v_report_conversions_total_dollar_monthly as
select
  ast.tenant_record_id,
  date_format(next_start_date, '%Y-%m-01') day,
  next_billing_period billing_period,
  round(sum(converted_next_price)) count
from analytics_subscription_transitions ast
join (
    select distinct tenant_record_id, bundle_id
    from analytics_invoice_items
    where invoice_original_amount_charged > 0
      and invoice_balance = 0
) paid_bundles
  on ast.bundle_id = paid_bundles.bundle_id
 and ast.tenant_record_id = paid_bundles.tenant_record_id
where report_group = 'default'
  and next_service = 'entitlement-service'
  and prev_phase = 'TRIAL'
  and next_phase != 'TRIAL'
  and event not like 'STOP_ENTITLEMENT%'
group by 1,2,3;
