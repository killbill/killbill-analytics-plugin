CREATE OR REPLACE VIEW v_report_refunds_monthly AS
select
  rfnd.account_name as "Customer Name"
, rfnd.account_external_key as "Account Number"
, rfnd.invoice_number as "Invoice Number"
, date_format(rfnd.invoice_date, '%m/%d/%Y') as "Invoice Date"
, rfnd.currency as "Currency"
, rfnd.invoice_original_amount_charged as "Invoice Amount"
, case when rfnd.currency != 'USD' then round(cc.reference_rate * rfnd.invoice_original_amount_charged,4) else rfnd.invoice_original_amount_charged end as "Invoice Amount USD"
, rfnd.plugin_name as "Payment Type"
, rfnd.plugin_pm_type as "Payment Method"
, rfnd.plugin_pm_cc_type as "Payment Card Type"
, date_format(rfnd.created_date, '%m/%d/%Y') as "Payment Date"
, rfnd.currency as "Payment Currency"
, rfnd.amount as "Payment Amount"
, case when rfnd.currency != 'USD' then round(cc.reference_rate * rfnd.amount,4) else rfnd.amount end as "Payment Amount USD"
, rfnd.record_id as "Refund Number"
, date_format(rfnd.created_date, '%m/%d/%Y') as "Refund Date"
, rfnd.currency as "Refund Currency"
, rfnd.amount as "Refund Amount"
, case when rfnd.currency != 'USD' then round(cc.reference_rate * rfnd.amount,4) else rfnd.amount end as "Refund Amount USD"
, rfnd.invoice_amount_charged - rfnd.invoice_original_amount_charged "Total IA, IIA for Invoice"
, case when rfnd.currency != 'USD' then round(cc.reference_rate * (rfnd.invoice_amount_charged - rfnd.invoice_original_amount_charged),4) else rfnd.invoice_amount_charged - rfnd.invoice_original_amount_charged end "Total IA, IIA for Invoice USD"
, rfnd.created_by as "User Responsible for Refund"
, 'REASON' as "Reason for Refund"
, rfnd.tenant_record_id
from
  analytics_payment_refunds rfnd
  left outer join analytics_currency_conversion cc on rfnd.created_date >= cc.start_date and rfnd.created_date <= cc.end_date and cc.currency = rfnd.currency
where 1=1
  and rfnd.created_date >= cast(date_format(date_sub(sysdate(), interval '1' month), '%Y-%m-01') as date)
  and rfnd.created_date < cast(date_format(sysdate(), '%Y-%m-01') as date)
  and rfnd.report_group != 'test'
order by 1,rfnd.invoice_payment_record_id; -- just for well defined ordering