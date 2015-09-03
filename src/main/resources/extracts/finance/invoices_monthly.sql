select
  inv.invoice_number as "Invoice Number"
, inv.account_name as "Customer Name"
, inv.account_external_key as "Account Number"
, date_format(inv.created_date, '%m/%d/%Y') as "Creation Date"
, date_format(inv.invoice_date, '%m/%d/%Y') as "Invoice Date"
, date_format(inv.invoice_date, '%m/%d/%Y') as "Target Date"
, inv.currency as "Currency"
, inv.original_amount_charged as "Invoice Amount"
, inv.balance as "Invoice Balance"
, round(cc.reference_rate * inv.original_amount_charged,4) as "Invoice Amount USD"
, round(cc.reference_rate * inv.balance,4) as "Invoice Balance USD"
from
  analytics_invoices inv
  join analytics_currency_conversion cc on inv.created_date >= cc.start_date and inv.created_date <= cc.end_date and cc.currency = inv.currency
where 1=1
  and inv.invoice_date >= cast(date_format(date_sub(sysdate(), interval '1' month), '%Y-%m-01') as date)
  and inv.invoice_date < cast(date_format(sysdate(), '%Y-%m-01') as date)
  and inv.report_group != 'test'
order by
  invoice_number
, inv.invoice_record_id;  -- just for well defined ordering
