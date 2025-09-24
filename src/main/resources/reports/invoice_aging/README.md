# Invoice Aging Report

Displays customer invoice balances by aging buckets in both original currency and USD, along with invoice and account details.

The snapshot view is: `v_report_invoice_aging`

## Prerequisites

Run stored proc

## Report Configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_invoice_aging",
          "reportType": "TABLE",
          "reportPrettyName": "Invoice Aging Report",
          "sourceTableName": "report_invoice_aging",
          "refreshProcedureName": "refresh_report_invoice_aging",
          "refreshFrequency": "DAILY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Report UI:

![invoice-aging.png](invoice-aging.png)
