select
  account_name "Customer Name"
, account_external_key "Account Number"
, a.currency "Currency"
, case when invoice_creation_date > '2013-08-01' - interval 30 day then invoice_original_amount_charged else 0 end as "Balance due 0-30 Days"
, case when invoice_creation_date between '2013-08-01' - interval 60 day and '2013-08-01' - interval 30 day then invoice_original_amount_charged else 0 end as "Balance due 30-60 Days"
, case when invoice_creation_date between '2013-08-01' - interval 90 day and '2013-08-01' - interval 60 day then invoice_original_amount_charged else 0 end as "Balance due 60-90 Days"
, case when invoice_creation_date between '2013-08-01' - interval 120 day and '2013-08-01' - interval 90 day then invoice_original_amount_charged else 0 end as "Balance due 90-120 Days"
, case when invoice_creation_date between '2013-08-01' - interval 150 day and '2013-08-01' - interval 120 day then invoice_original_amount_charged else 0 end as "Balance due 120-150 Days"
, case when invoice_creation_date < '2013-08-01' - interval 150 day then invoice_original_amount_charged else 0 end as "Balance due 150+ Days"
, invoice_original_amount_charged as "Total Balance Due"
, case when invoice_creation_date > '2013-08-01' - interval 30 day then round(cc.reference_rate * invoice_original_amount_charged,4) else 0 end as "Balance due 0-30 Days USD"
, case when invoice_creation_date between '2013-08-01' - interval 60 day and '2013-08-01' - interval 30 day then round(cc.reference_rate * invoice_original_amount_charged,4) else 0 end as "Balance due 30-60 Days USD"
, case when invoice_creation_date between '2013-08-01' - interval 90 day and '2013-08-01' - interval 60 day then round(cc.reference_rate * invoice_original_amount_charged,4) else 0 end as "Balance due 60-90 Days USD"
, case when invoice_creation_date between '2013-08-01' - interval 120 day and '2013-08-01' - interval 90 day then round(cc.reference_rate * invoice_original_amount_charged,4) else 0 end as "Balance due 90-120 Days USD"
, case when invoice_creation_date between '2013-08-01' - interval 150 day and '2013-08-01' - interval 120 day then round(cc.reference_rate * invoice_original_amount_charged,4) else 0 end as "Balance due 120-150 Days USD"
, case when invoice_creation_date < '2013-08-01' - interval 150 day then round(cc.reference_rate * invoice_original_amount_charged,4) else 0 end as "Balance due 150+ Days USD"
, cc.reference_rate * invoice_original_amount_charged as "Total Balance Due USD"
, invoice_number "Invoice Number"
, bundle_external_key "App ID"
, slug "Slug"
, service_start_date "Service Start Date"
, service_end_date "Service End Date"
, invoice_date "Invoice Date"
, invoice_original_amount_charged "Invoice Amount"
, invoice_balance "Invoice Balance"
, round(cc.reference_rate * invoice_original_amount_charged,4) "Invoice Amount USD"
, round(cc.reference_rate * invoice_balance,4) "Invoice Balance USD"
from (
    select
      ii.invoice_number
    , ii.account_name
    , ii.account_external_key
    , date_format(ii.created_date, '%m/%d/%Y') invoice_creation_date
    , date_format(ii.invoice_date, '%m/%d/%Y') invoice_date
    , date_format(ii.invoice_date, '%m/%d/%Y') target_date
    , ii.bundle_external_key
    , ii.product_name
    , ii.slug
    , date_format(ii.start_date, '%m/%d/%Y') as service_start_date
    , date_format(ii.end_date, '%m/%d/%Y') as service_end_date
    , ii.currency as "Currency"
    , ii.invoice_original_amount_charged
    , ii.invoice_balance
    , ii.amount
    , ii.created_date
    , ii.invoice_item_record_id
    from analytics_invoice_items ii
    where 1=1
      and ii.invoice_date < date_format(sysdate(),'%Y-%m-01')
      and ii.report_group = 'default'
      and ii.invoice_balance > 0
) a
join analytics_currency_conversion cc on a.created_date >= cc.start_date and a.created_date <= cc.end_date and cc.currency =a.currency
order by
  account_name
, invoice_number
, a.invoice_item_record_id;
