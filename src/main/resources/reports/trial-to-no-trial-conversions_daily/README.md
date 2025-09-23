# Daily Trials To No Trials Count Report

Count of subscriptions converting from trial to non-trial per tenant per day.

The snapshot view is: [v_report_trial_to_no_trial_conversions_daily](v_report_trial_to_no_trial_conversions_daily.ddl)

## Timeline configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_trial_to_no_trial_conversions_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily Trials to No Trials Count",
          "sourceTableName": "report_trial_to_no_trial_conversions_daily",
          "refreshProcedureName": "refresh_report_trial_to_no_trial_conversions_daily",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Sample Data

| Tenant Record Id | Day        | Count |
|------------------|------------|-------|
| 1                | 2025-06-04 | 1     |
| 1                | 2025-10-18 | 3     |
| 22               | 2025-08-07 | 1     |
| 22               | 2025-07-12 | 1     |
| 1                | 2025-01-31 | 1     |
| 45               | 2025-04-04 | 1     |
| 489              | 2025-09-05 | 1     |

This means that on `2025-10-18`, 3 subscriptions transitioned from the `TRIAL` phase to some other phase for the `tenant_record_id=1`.


## Report UI:

![trial-to-no-trial-conversions.png](trial-to-no-trial-conversions.png)



