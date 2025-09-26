# Daily Invoices Report

Compute the total invoice amount charged (in the reference currency) per day per currency.

The snapshot view is: `v_report_invoices_daily`

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_invoices_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily Invoices Value",
          "sourceTableName": "report_invoices_daily",
          "refreshProcedureName": "refresh_report_invoices_daily",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Sample Data

| ID  | Date       | Currency | Amount    |
|-----|------------|----------|-----------|
| 1   | 2025-09-05 | USD      | 579.0700  |
| 1   | 2025-09-19 | USD      | 728.4000  |
| 1   | 2025-09-22 | USD      | 100.0000  |
| 489 | 2025-09-21 | USD      | 250.0000  |
| 1   | 2025-09-25 | EUR      | 59.9500   |
| 359 | 2025-09-25 | USD      | 1009.3000 |
| 1   | 2025-09-11 | USD      | 516.9500  |
| 1   | 2025-09-28 | EUR      | 0.0000    |
| 359 | 2025-09-28 | USD      | 356.6900  |

The first row in the above table indicates that on the date `2025-09-05`, the tenant with record id=1 had a total invoice value of USD 579.0700.


## Report UI:

![invoice-amount-daily.png](invoice-amount-daily.png)