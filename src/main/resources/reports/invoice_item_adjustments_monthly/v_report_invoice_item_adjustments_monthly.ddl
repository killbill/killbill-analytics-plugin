CREATE OR REPLACE VIEW v_report_invoice_item_adjustments_monthly AS
select
  iia.invoice_number as "Invoice Number"
, iia.account_name as "Customer Name"
, iia.account_external_key as "Account Number"
, date_format(iia.created_date, '%m/%d/%Y') as "Creation Date" -- Adjustment date?
, date_format(iia.invoice_date, '%m/%d/%Y') as "Invoice Date"
, date_format(iia.invoice_date, '%m/%d/%Y') as "Target Date"
, iia.bundle_external_key as "Bundle External Key"
, iia.product_name as "Product"
, iia.slug as "Slug"
, date_format(iia.start_date, '%m/%d/%Y') as "Service Start Date"
, date_format(iia.end_date, '%m/%d/%Y') as "Service End Date"
, iia.currency as "Currency"
, iia.invoice_original_amount_charged as "Invoice Amount"
, iia.invoice_balance as "Invoice Balance"
, iia.amount as "Invoice Item Adjustment Amount"
, abs(iia.amount) as "Impact Amount"
, case when iia.currency != 'USD' then round(cc.reference_rate * iia.invoice_original_amount_charged,4) else iia.invoice_original_amount_charged END as "Invoice Amount USD"
, case when iia.currency != 'USD' then round(cc.reference_rate *  iia.invoice_balance,4) else iia.invoice_balance end as "Invoice Balance USD"
, case when iia.currency != 'USD' then round(cc.reference_rate * iia.amount,4) else iia.amount end as "Invoice Item Adjustment Amount USD"
, case when iia.currency != 'USD' then abs(round(cc.reference_rate * iia.amount,4)) else abs(iia.amount) end as "Impact Amount USD"
, case when iia.amount < 0 then 'CREDIT' else 'CHARGE' end as "Adjustment Type"
, 'PROCESSED' as "Invoice Item Adjustment Status"
, iia.tenant_record_id
from
  analytics_invoice_item_adjustments iia
  join analytics_invoice_items ii on iia.linked_item_id = ii.item_id -- workaround
  left outer join analytics_currency_conversion cc on iia.created_date >= cc.start_date and iia.created_date <= cc.end_date and cc.currency = iia.currency
where 1=1
  and iia.created_date >= cast(date_format(date_sub(sysdate(), interval '1' month), '%Y-%m-01') as date)
  and iia.created_date < cast(date_format(sysdate(), '%Y-%m-01') as date)
  and iia.report_group != 'test'
order by
  iia.invoice_number
, iia.invoice_item_record_id; -- just for well defined ordering
