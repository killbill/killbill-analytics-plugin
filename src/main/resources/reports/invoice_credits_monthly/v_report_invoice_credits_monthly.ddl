CREATE OR REPLACE VIEW v_report_invoice_credits_monthly AS
select
  ic.invoice_number as "Invoice Number"
, ic.account_name as "Customer Name"
, ic.account_external_key as "Account Number"
, date_format(ic.created_date, '%m/%d/%Y') as "Creation Date"
, date_format(ic.invoice_date, '%m/%d/%Y') as "Invoice Date"
, date_format(ic.invoice_date, '%m/%d/%Y') as "Target Date"
, ic.bundle_external_key as "Bundle External Key"
, ic.product_name as "Product"
, ic.slug as "Slug"
, date_format(ic.start_date, '%m/%d/%Y') as "Service Start Date"
, date_format(ic.end_date, '%m/%d/%Y') as "Service End Date"
, ic.currency as "Currency"
, ic.invoice_original_amount_charged as "Invoice Amount"
, ic.invoice_balance as "Invoice Balance"
, ic.amount as "Invoice Credit Amount"
, case when ic.currency != 'USD' THEN round(cc.reference_rate * ic.invoice_original_amount_charged,4) else invoice_original_amount_charged end as "Invoice Amount USD"
, case when ic.currency != 'USD' THEN round(cc.reference_rate * ic.invoice_balance,4) else invoice_balance end as "Invoice Balance USD"
, case when ic.currency != 'USD' THEN round(cc.reference_rate * ic.amount,4) else amount end as "Invoice Credit Amount USD"
, ic.tenant_record_id
from
  analytics_invoice_credits ic
  left outer join analytics_currency_conversion cc on ic.created_date >= cc.start_date and ic.created_date <= cc.end_date and cc.currency =ic.currency
where 1=1
  and ic.created_date >= cast(date_format(date_sub(sysdate(), interval '1' month), '%Y-%m-01') as date)
  and ic.created_date < cast(date_format(sysdate(), '%Y-%m-01') as date)
  and ic.report_group != 'test'
order by
  invoice_number
, ic.invoice_item_record_id; -- just for well defined ordering