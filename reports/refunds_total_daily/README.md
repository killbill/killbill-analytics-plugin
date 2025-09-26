# Daily Refunds Report

Compute the total value (in the reference currency) of refunds per day per currency for each tenant.

The snapshot view is: `v_report_refunds_total_daily`

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_refunds_total_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily Refunds Value",
          "sourceTableName": "report_refunds_total_daily",
          "refreshProcedureName": "refresh_report_refunds_total_daily",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Sample Data

| ID | Date       | Currency | Amount   |
|----|------------|----------|----------|
| 1  | 2025-09-02 | USD      | 119.0000 |
| 1  | 2025-08-04 | USD      | 29.9500  |
| 1  | 2025-08-04| EUR      | 199.4000 |
| 2  | 2025-07-03 | USD      | 29.9500  |
| 2  | 2025-09-18 | EUR      | 15.0000  |

The first row in the above table indicates that on the date `2025-09-02`, the tenant with record id=1 had a total refund value of USD 119.

## Report UI:

![refunds-total-daily.png](refunds-total-daily.png)
