# Payment Provider Errors report

Compute the top errors per provider and currency, per day.

The snapshot view is: `v_report_payment_provider_errors`

## Timeline configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_payment_provider_errors",
          "reportType": "TIMELINE",
          "reportPrettyName": "Payment Provider Errors",
          "sourceTableName": "report_payment_provider_errors",
          "refreshProcedureName": "refresh_report_payment_provider_errors",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
