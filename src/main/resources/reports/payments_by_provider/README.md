# Payments By Provider report

Compute the number of payments by  transaction state over different timeframes for each payment service provider (plugin).

The snapshot view is: `v_report_payments_by_provider`

## History table configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_payments_by_provider",
          "reportType": "TABLE",
          "reportPrettyName": "Payments By Provider",
          "sourceTableName": "report_payments_by_provider_history",
          "refreshProcedureName": "refresh_report_payments_by_provider_history",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Summary pie charts configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_payments_by_provider_last_24h_summary",
          "reportType": "COUNTERS",
          "reportPrettyName": "Payments By Provider Summary (last 24hrs)",
          "sourceTableName": "v_report_payments_by_provider_last_24h_summary"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
