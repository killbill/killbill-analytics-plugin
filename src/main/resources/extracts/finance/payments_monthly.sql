select
  pmt.account_name as "Customer Name"
, pmt.account_external_key as "Account Number"
, pmt.invoice_number as "Invoice Number"
, date_format(pmt.invoice_date, '%m/%d/%Y') as "Invoice Date"
, pmt.currency as "Currency"
, pmt.invoice_original_amount_charged as "Invoice Amount"
, round(cc.reference_rate * pmt.invoice_original_amount_charged,4) as "Invoice Amount USD"
, pmt.plugin_name as "Payment Gateway"
, pmt.plugin_pm_type as "Payment Method"
, pmt.plugin_pm_cc_type as "Payment Card Type"
, pmt.plugin_first_reference_id as "Primary Payment Reference ID"
, pmt.plugin_second_reference_id as "Secondary Payment Reference ID"
, date_format(pmt.created_date, '%m/%d/%Y') as "Payment Date"
, pmt.currency as "Payment Currency"
, pmt.amount as "Payment Amount"
, round(cc.reference_rate * pmt.amount,4) as "Payment Amount USD"
, pmt.payment_id
from
  analytics_payments pmt
  join analytics_currency_conversion cc on pmt.created_date >= cc.start_date and pmt.created_date <= cc.end_date and cc.currency =pmt.currency
where 1=1
  and pmt.created_date >= date_format(date_sub(sysdate(), interval 1 month),'%Y-%m-01')
  and pmt.created_date < date_format(sysdate(),'%Y-%m-01')
  and pmt.report_group = 'default'
order by
  account_name
, pmt.invoice_payment_record_id; -- just for well defined ordering
