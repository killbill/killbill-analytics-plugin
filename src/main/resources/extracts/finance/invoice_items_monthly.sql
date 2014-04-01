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
, round(cc.reference_rate * ii.invoice_original_amount_charged,4) as "Invoice Amount USD"
, round(cc.reference_rate * ii.invoice_balance,4) as "Invoice Balance USD"
, round(cc.reference_rate * ii.amount,4) as "Invoice Item Amount USD"
from
  analytics_invoice_items ii
  join analytics_currency_conversion cc on ii.created_date >= cc.start_date and ii.created_date <= cc.end_date and cc.currency =ii.currency
where 1=1
  and ii.invoice_date >= date_format(date_sub(sysdate(), interval 1 month),'%Y-%m-01')
  and ii.invoice_date < date_format(sysdate(),'%Y-%m-01')
  and ii.report_group != 'test'
order by
  invoice_number
, ii.invoice_item_record_id; -- just for well defined ordering
