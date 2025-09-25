CREATE OR REPLACE VIEW v_report_invoice_items_monthly AS
select
  ii.invoice_number as "Invoice Number"
, ii.account_name as "Customer Name"
, ii.account_external_key as "Account Number"
, date_format(ii.created_date, '%m/%d/%Y') as "Creation Date"
, date_format(ii.invoice_date, '%m/%d/%Y') as "Invoice Date"
, date_format(ii.invoice_date, '%m/%d/%Y') as "Target Date"
, ii.bundle_external_key as "Bundle External Key"
, ii.product_name as "Product"
, ii.slug as "Slug"
, date_format(ii.start_date, '%m/%d/%Y') as "Service Start Date"
, date_format(ii.end_date, '%m/%d/%Y') as "Service End Date"
, ii.currency as "Currency"
, ii.invoice_original_amount_charged as "Invoice Amount"
, ii.invoice_balance as "Invoice Balance"
, ii.amount as "Invoice Item Amount"
, case when ii.currency != 'USD' THEN round(cc.reference_rate * ii.invoice_original_amount_charged,4) else ii.invoice_original_amount_charged END  as "Invoice Amount USD"
, case when ii.currency != 'USD' THEN round(cc.reference_rate * ii.invoice_balance,4) else ii.invoice_balance END as "Invoice Balance USD"
, case when ii.currency != 'USD' THEN round(cc.reference_rate * ii.amount,4) else ii.amount END as "Invoice Item Amount USD"
, ii.tenant_record_id
from
  analytics_invoice_items ii
  left outer join analytics_currency_conversion cc on ii.created_date >= cc.start_date and ii.created_date <= cc.end_date and cc.currency = ii.currency
where 1=1
  and ii.invoice_date >= cast(date_format(sysdate() -  interval '1' month, '%Y-%m-01') as date)
  and ii.invoice_date < cast(date_format(sysdate(), '%Y-%m-01') as date)
  and ii.report_group != 'test'
order by
  invoice_number
, ii.invoice_item_record_id; -- just for well defined ordering