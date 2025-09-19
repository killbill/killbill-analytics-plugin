# Daily cancellations report

Compute the total number of cancellations per day per phase.

The snapshot view is: `v_report_cancellations_daily`

## Timeline configuration

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
          "refreshFrequency": "DAILY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
## Report UI:

![daily-cancellations.png](daily-cancellations.png)