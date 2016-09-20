# Payment Provider Conversion report

Compare the total number of transactions and the number of successful transactions that have occurred in a recent 15 minutes 
period to the same metrics that occurred in the corresponding 15 minutes from 14 days ago.

The snapshot view is: `v_report_payment_provider_conversion`.

## History table configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_payment_provider_conversion",
          "reportType": "TABLE",
          "reportPrettyName": "Payment Provider Conversion",
          "sourceTableName": "report_payment_provider_conversion_history",
          "refreshProcedureName": "refresh_report_payment_provider_conversion_history",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
