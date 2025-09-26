# Invoice Aging No Payments Report

This report lists all customer invoices with no payments recorded, categorizing outstanding amounts into standard aging buckets and converting balances into USD for comparison.

The snapshot view is: `v_report_invoice_aging_no_payment`

## Prerequisites

This report requires the `analytics_currency_conversion` table to be populated. See [insertMonthlyCurrencyConversionRates.ddl](../utils/insertMonthlyCurrencyConversionRates.ddl)

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_invoice_aging_no_payment",
          "reportType": "TABLE",
          "reportPrettyName": "Invoice Aging No Payments Report",
          "sourceTableName": "report_invoice_aging_no_payment",
          "refreshProcedureName": "refresh_report_invoice_aging_no_payment",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Report UI:

![invoice-aging-no-payment.png](invoice-aging-no-payment.png)
