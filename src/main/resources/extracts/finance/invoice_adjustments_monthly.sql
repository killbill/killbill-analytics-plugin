select
  ia.invoice_number as "Invoice Number"
, ia.account_name as "Customer Name"
, ia.account_external_key as "Account Number"
, date_format(ia.created_date, '%m/%d/%Y') as "Adjustment Date"
, date_format(ia.invoice_date, '%m/%d/%Y') as "Invoice Date"
, date_format(ia.invoice_date, '%m/%d/%Y') as "Target Date"
, date_format(ia.created_date, '%m/%d/%Y') as "Creation Date" -- Adjustment date?
, ia.currency as "Currency"
, ia.invoice_original_amount_charged as "Invoice Amount"
, ia.invoice_balance as "Invoice Balance"
, ia.amount as "Invoice Adjustment Amount"
, abs(ia.amount) as "Impact Amount"
, round(cc.reference_rate * ia.invoice_original_amount_charged,4) as "Invoice Amount USD"
, round(cc.reference_rate * ia.invoice_balance,4) as "Invoice Balance USD"
, round(cc.reference_rate * ia.amount,4) as "Invoice Adjustment Amount USD"
, abs(round(cc.reference_rate * ia.amount,4)) as "Impact Amount USD"
, case when ia.amount > 0 then 'CREDIT' else 'CHARGE' end as "Adjustment Type"
, 'PROCESSED' as "Invoice Adjustment Status"
from
  analytics_invoice_adjustments ia
  join analytics_currency_conversion cc on ia.created_date >= cc.start_date and ia.created_date <= cc.end_date and cc.currency =ia.currency
where 1=1
  and ia.created_date >= date_format(date_sub(sysdate(), interval 1 month),'%Y-%m-01')
  and ia.created_date < date_format(sysdate(),'%Y-%m-01')
  and ia.report_group = 'default'
order by
  invoice_number
, ia.record_id; -- just for well defined ordering
