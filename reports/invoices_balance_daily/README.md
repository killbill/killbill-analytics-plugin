# Daily Invoice Balance Report

Compute the total sum of invoices balance (in the reference currency) per invoice created day.

The snapshot view is: `v_report_invoices_balance_daily`

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_invoices_balance_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily Invoices Balance",
          "sourceTableName": "report_invoices_balance_daily",
          "refreshProcedureName": "refresh_report_invoices_balance_daily",
          "refreshFrequency": "DAILY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
## Report UI:

![invoice-balance-daily.png](invoice-balance-daily.png)