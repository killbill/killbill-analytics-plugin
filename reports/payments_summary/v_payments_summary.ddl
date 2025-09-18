create or replace view v_report_payments_summary as
select app.payment_number as PaymentNumber,
app.payment_id as PaymentID,
app.created_date as PaymentDate,
app.payment_transaction_status as Status,
app.account_id as AccountID,
aa.email as AccountEmail,
app.account_external_key as AccountExternalKey,
app.plugin_name as PaymentProvider,
app.amount as PaymentAmount,
app.currency as Currency,
app.tenant_record_id,
date_format(app.created_date,'%Y-%m-%d') as day
from analytics_payment_purchases app
left join analytics_accounts aa on app.account_id=aa.account_id