select
  rfnd.account_name as "Customer Name"
, rfnd.account_external_key as "Account Number"
, rfnd.invoice_number as "Invoice Number"
, date_format(rfnd.invoice_date, '%m/%d/%Y') as "Invoice Date"
, rfnd.currency as "Currency"
, rfnd.invoice_original_amount_charged as "Invoice Amount"
, round(cc.reference_rate * rfnd.invoice_original_amount_charged,4) as "Invoice Amount USD"
, rfnd.plugin_name as "Payment Type"
, rfnd.plugin_pm_type as "Payment Method"
, rfnd.plugin_pm_cc_type as "Payment Card Type"
, case when plugin_name = 'killbill-litle' and plugin_pm_cc_type != 'AmericanExpress' then upper(lr.params_litleonelineresponse_saleresponse_litle_txn_id) else null end as "Litle Payment Reference ID" -- workaround
, case when plugin_name = 'killbill-litle' and plugin_pm_cc_type = 'AmericanExpress' then upper(lr.params_litleonelineresponse_saleresponse_id) else null end as "AmericanExpress Payment Reference ID" -- workaround
, case when plugin_name = 'killbill-paypal-express' then pp.paypal_express_txn_id else null end as "PayPal Payment Reference ID" -- workaround
, date_format(rfnd.created_date, '%m/%d/%Y') as "Payment Date"
, rfnd.currency as "Payment Currency"
, rfnd.amount as "Payment Amount"
, round(cc.reference_rate * rfnd.amount,4) as "Payment Amount USD"
, rfnd.record_id as "Refund Number"
, date_format(rfnd.created_date, '%m/%d/%Y') as "Refund Date"
, rfnd.currency as "Refund Currency"
, rfnd.amount as "Refund Amount"
, round(cc.reference_rate * rfnd.amount,4) as "Refund Amount USD"
, rfnd.invoice_amount_charged - rfnd.invoice_original_amount_charged "Total IA, IIA for Invoice"
, round(cc.reference_rate * (rfnd.invoice_amount_charged - rfnd.invoice_original_amount_charged),4) "Total IA, IIA for Invoice"
, rfnd.created_by as "User Responsible for Refund"
, 'REASON' as "Reason for Refund"
from
  analytics_refunds rfnd
  join analytics_currency_conversion cc on rfnd.created_date >= cc.start_date and rfnd.created_date <= cc.end_date and cc.currency =rfnd.currency
  left outer join paypal_express_transactions pp on pp.kb_payment_id=rfnd.payment_id and api_call = 'refund' -- workaround paypal #
  left outer join litle_responses lr on lr.kb_payment_id=rfnd.payment_id and lr.success=1 and lr.api_call = 'charge' -- workaround litle #     
where 1=1
  and rfnd.created_date >= date_format(date_sub(sysdate(), interval 1 month),'%Y-%m-01')
  and rfnd.created_date < date_format(sysdate(),'%Y-%m-01')
  and rfnd.report_group != 'test'
order by 1,rfnd.invoice_payment_record_id; -- just for well defined ordering
