# Daily Cancellations Report

Compute the total number of cancellations per day per phase.

The snapshot view is: `v_report_cancellations_daily`

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_cancellations_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily Cancellations",
          "sourceTableName": "report_cancellations_daily",
          "refreshProcedureName": "refresh_report_cancellations_daily",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
## Sample Data

| tenant_record_id | phase     | day        | count |
|------------------|-----------|------------|-------|
| 1                | EVERGREEN | 2025-09-10 | 9     |
| 1                | EVERGREEN | 2025-09-03 | 2     |
| 1                | EVERGREEN | 2025-09-22 | 5     |
| 2                | EVERGREEN | 2025-09-23 | 6     |
| 2                | EVERGREEN | 2025-09-24 | 6     |
| 1                | EVERGREEN | 2025-09-25 | 1     |

The first row in the above table indicates that on `2025-09-10`, the `tenant_record_id=1` had 9 cancellations in `EVERGREEN` phase.

## Report UI:

![daily-cancellations.png](daily-cancellations.png)