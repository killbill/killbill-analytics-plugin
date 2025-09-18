create or replace view v_report_bundles_summary as
select
    ab.tenant_record_id,
    ab.bundle_id as BundleId,
    ab.account_id as AccountID,
    ab.account_external_key as AccountExternalKey,
    aa.email as AccountEmail,
    ab.created_date as CreatedDate,
    ab.current_start_date as StartDate,
    ab.charged_through_date as ChargedThroughDate,
    date_format(ab.created_date,'%Y-%m-%d') as day,
    case
        when ab.current_state in ('ENT_STARTED', 'START_BILLING') then 'ACTIVE'
        when ab.current_state = 'STOP_BILLING' then 'CANCELLED'
        else ab.current_state
    end as Status,
    -- Remove "-<phase>" from current_slug
    left(ab.current_slug, length(ab.current_slug) - length(ab.current_phase) - 1) as PlanName,
    ab.current_price as Price,
    aa.currency as Currency
from analytics_bundles ab
left join analytics_accounts aa
  on ab.account_record_id = aa.account_record_id;

