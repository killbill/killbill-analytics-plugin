# Daily Trials Count Report

Count of new trial subscriptions per tenant, per day and per product.

The snapshot view is: [v_report_trial_starts_count_daily](v_report_trial_starts_count_daily.md)

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_trials_start_count_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily Trials Count",
          "sourceTableName": "report_trial_starts_count_daily",
          "refreshProcedureName": "refresh_report_trial_starts_count_daily",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

### Sample Data

| Tenant Record Id | Day        | Product       | Count |
|------------------|------------|---------------|-------|
| 1                | 2025-07-01 | Assault-Rifle | 1     |
| 1                | 2025-05-05 | Pistol        | 5     |
| 22               | 2025-09-18 | Pistol        | 2     |
| 22               | 2025-07-01 | Pistol        | 1     |
| 44               | 2025-07-08 | Blowdart      | 8     |
| 44               | 2025-06-12 | Pistol        | 6     |

This means that on `2025-09-18`, 2 new subscriptions were started in the `TRIAL` phase for the `Pistol` product and `tenant_record_id=1`.

## Report UI:

![daily-trials-count.png](daily-trials-count.png)


