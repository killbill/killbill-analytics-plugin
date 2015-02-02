drop procedure if exists refresh_report_subscriptions_total_dollar_monthly;
DELIMITER //
CREATE PROCEDURE refresh_report_subscriptions_total_dollar_monthly()
BEGIN

    DELETE FROM report_subscriptions_total_dollar_monthly;

    create temporary table report_temp_paid_bundles (index (bundle_id)) as
       select distinct
         tenant_record_id
       , bundle_id
       from
         analytics_invoice_items
       where 1=1
         and invoice_original_amount_charged > 0
         and invoice_balance = 0
    ;

    insert into report_subscriptions_total_dollar_monthly
    select
      ast.tenant_record_id
    , cal.d month
    , next_billing_period term
    , round(sum(case when next_billing_period ='ANNUAL' then converted_next_price/12 else converted_next_price end))
    from
      analytics_subscription_transitions ast
      join calendar cal on next_start_date < cal.d and (next_end_date > cal.d or next_end_date is null) and (cal.d = date_format(cal.d, '%Y-%m-01')) and cal.d>='2013-01-01' and cal.d < sysdate()
      join report_temp_paid_bundles paid_bundles on ast.bundle_id = paid_bundles.bundle_id and ast.tenant_record_id = paid_bundles.tenant_record_id
    where 1=1
      and report_group='default'
      and next_service='entitlement-service'
      and event not like 'STOP_ENTITLEMENT%'
      and next_billing_period in ('ANNUAL','MONTHLY')
    group by 1,2,3
    ;

END;
//
DELIMITER ;
