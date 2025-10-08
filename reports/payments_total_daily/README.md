# Daily Payments Report

Compute the total value (in the reference currency) of payments per day per currency.

The snapshot view is: `v_report_payments_total_daily`

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_payments_total_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily Payments Value",
          "sourceTableName": "report_payments_total_daily",
          "refreshProcedureName": "refresh_report_payments_total_daily",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Sample Data

| tenant_record_id | day        | currency | amount   |
|------------------|------------|----------|----------|
| 1                | 2025-05-01 | USD      | 49.9000  |
| 1                | 2025-05-05 | USD      | 26.0900  |
| 1                | 2025-05-05 | EUR      | 79.8500  |
| 2                | 2025-07-03 | USD      | 229.6000 |
| 2                | 2025-07-20 | EUR      | 379.9000 |

The first row in the above table indicates that on the date `2025-05-01`, the tenant with record id=1 had a total payment value of USD 49.9.

## Report UI:

![payments-total-daily.png](payments-total-daily.png)