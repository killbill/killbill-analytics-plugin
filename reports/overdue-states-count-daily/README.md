# Daily Overdue Count Report

Count of overdue states per tenant and per day.

The snapshot view is: [v_report_overdue_states_count_daily](v_report_overdue_states_count_daily.ddl)

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_overdue_states_count_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily Overdue Count",
          "sourceTableName": "report_overdue_states_count_daily",
          "refreshProcedureName": "refresh_report_overdue_states_count_daily",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Sample Data

| Tenant Record Id | State        | Day    | Count |
|------------------|--------------|------------|-------|
| 1                | BLOCKED      | 2025-09-15 | 5     |
| 1                | BLOCKED      | 2025-09-16 | 5     |
| 22               | BLOCKED      | 2025-09-17 | 6     |
| 22               | CANCELLATION | 2025-09-18 | 3     |
| 1                | BLOCKED      | 2025-09-18 | 1     |
| 23               | BLOCKED      | 2025-09-19 | 7     |
| 45               | CANCELLATION | 2025-09-19 | 5     |


The first row in the above table indicates that on the date `2025-09-15`, the tenant with record id=1 had 5 accounts in the `BLOCKED` overdue state.

## Report UI:

![overdue-states-count-daily.png](overdue-states-count-daily.png)



