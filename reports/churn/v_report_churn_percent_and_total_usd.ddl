create or replace view v_report_churn_percent_and_total_usd as
with paid_bundles_monthly as (
    select distinct tenant_record_id, bundle_id
    from analytics_invoice_items
    where invoice_original_amount_charged > 0
      and invoice_balance = 0
),
paid_bundles_annual as (
    select distinct tenant_record_id, bundle_id
    from (
        select tenant_record_id, bundle_id
        from analytics_invoice_items
        where invoice_original_amount_charged > 0
          and invoice_balance = 0
        union
        select s.tenant_record_id, s.bundle_id
        from subscription_events se
        join subscriptions s
          on se.subscription_id = s.id
         and se.tenant_record_id = s.tenant_record_id
        where user_type in ('MIGRATE_ENTITLEMENT')
    ) bundles
),
paid_bundles_annual2 as (
    select distinct tenant_record_id, bundle_id, charged_through_date
    from (
        select tenant_record_id, bundle_id, end_date as charged_through_date
        from analytics_invoice_items
        where invoice_original_amount_charged > 0
          and invoice_balance = 0
        union
        select s.tenant_record_id, s.bundle_id, effective_date as charged_through_date
        from subscription_events se
        join subscriptions s
          on se.subscription_id = s.id
         and se.tenant_record_id = s.tenant_record_id
        where user_type in ('MIGRATE_ENTITLEMENT')
    ) bundles
)
-- ============================
-- MONTHLY PART
-- ============================
select
  active_sub_dollar.tenant_record_id,
  active_sub_dollar.month,
  round(churn_dollar.amount) as churn_dollars,
  round(churn_dollar.amount / active_sub_dollar.amount, 4) as churn_pct,
  'MONTHLY' as billing_period
from (
  select
    ast.tenant_record_id,
    date_format(next_start_date, '%Y-%m-01') as month,
    prev_billing_period,
    sum(converted_prev_price) as amount
  from analytics_subscription_transitions ast
  join paid_bundles_monthly b
    on ast.bundle_id = b.bundle_id
   and ast.tenant_record_id = b.tenant_record_id
  where report_group = 'default'
    and next_service = 'entitlement-service'
    and event like 'STOP_ENTITLEMENT%'
    and prev_billing_period in ('MONTHLY')
  group by 1,2,3
) churn_dollar
join (
  select
    ast.tenant_record_id,
    cal.d as month,
    next_billing_period,
    sum(converted_next_price) as amount
  from analytics_subscription_transitions ast
  join calendar cal
    on next_start_date < cal.d
   and (next_end_date > cal.d or next_end_date is null)
   and cal.d = date_format(cal.d, '%Y-%m-01')
   and cal.d >= '2013-01-01'
   and cal.d < sysdate()
  join paid_bundles_monthly b
    on ast.bundle_id = b.bundle_id
   and ast.tenant_record_id = b.tenant_record_id
  where report_group = 'default'
    and next_service = 'entitlement-service'
    and event not like 'STOP_ENTITLEMENT%'
    and next_billing_period in ('MONTHLY')
  group by 1,2,3
) active_sub_dollar
  on churn_dollar.month = active_sub_dollar.month
 and churn_dollar.prev_billing_period = active_sub_dollar.next_billing_period
 and churn_dollar.tenant_record_id = active_sub_dollar.tenant_record_id

union all

-- ============================
-- ANNUAL PART
-- ============================
select
  churn_dollar.tenant_record_id,
  churn_dollar.month,
  churn_dollar.amount as churn_dollars,
  round(churn_dollar.amount / active_sub_dollar.amount, 4) as churn_pct,
  'ANNUAL' as billing_period
from (
  select
    ast.tenant_record_id,
    date_format(next_start_date, '%Y-%m-01') as month,
    prev_billing_period,
    round(sum(converted_prev_price)) as amount
  from analytics_subscription_transitions ast
  join paid_bundles_annual b
    on ast.bundle_id = b.bundle_id
   and ast.tenant_record_id = b.tenant_record_id
  where report_group = 'default'
    and next_service = 'entitlement-service'
    and event like 'STOP_ENTITLEMENT%'
    and prev_billing_period in ('ANNUAL')
  group by 1,2,3
) churn_dollar
join (
  select
    ast.tenant_record_id,
    cal.d as month,
    next_billing_period,
    round(sum(converted_next_price)) as amount
  from analytics_subscription_transitions ast
  join calendar cal
    on next_start_date < cal.d
   and (next_end_date > cal.d or next_end_date is null)
   and cal.d = date_format(cal.d, '%Y-%m-01')
   and cal.d >= '2013-01-01'
   and cal.d < sysdate()
  join paid_bundles_annual2 b
    on ast.bundle_id = b.bundle_id
   and ast.tenant_record_id = b.tenant_record_id
  where report_group = 'default'
    and next_service = 'entitlement-service'
    and event not like 'STOP_ENTITLEMENT%'
    and next_billing_period in ('ANNUAL')
    and extract(month from date_add(charged_through_date, interval 1 day)) = extract(month from cal.d)
  group by 1,2,3
) active_sub_dollar
  on churn_dollar.month = active_sub_dollar.month
 and churn_dollar.prev_billing_period = active_sub_dollar.next_billing_period
 and churn_dollar.tenant_record_id = active_sub_dollar.tenant_record_id;
