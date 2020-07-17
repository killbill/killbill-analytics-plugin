# Daily refunds report

Compute the total value (in the reference currency) of refunds per day per currency.

The snapshot view is: `v_report_refunds_total_daily`

## Timeline configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_refunds_total_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily refunds value",
          "sourceTableName": "report_refunds_total_daily",
          "refreshProcedureName": "refresh_report_refunds_total_daily",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
