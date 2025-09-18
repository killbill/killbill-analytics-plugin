create or replace view v_report_accounts_summary as
select
    aa.tenant_record_id,
    aa.account_id as AccountID,
    aa.email as Email,
    aa.created_date as CreatedDate,
    aa.nb_active_bundles as ActiveBundlesCount,
    aa.balance as AccountBalance,
    aa.billing_cycle_day_local as BCD,
    aa.currency as Currency,
    pm.plugin_name as PaymentMethodName,
    aat.state as AccountStatus,
    date_format(aa.created_date,'%Y-%m-%d') as day,
    n.effective_date as NextInvoiceDate
from analytics_accounts aa
left join payment_methods pm
    on aa.payment_method_id = pm.id
left join analytics_account_transitions aat
    on aa.account_id = aat.account_id
   and aat.created_date = (
       select max(created_date)
       from analytics_account_transitions
       where account_id = aa.account_id
   )
left join notifications n
on aa.account_record_id=n.search_key1
and n.class_name='org.killbill.billing.invoice.notification.NextBillingDateNotificationKey';
