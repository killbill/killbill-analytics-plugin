# Payments Summary Report

Provides payment summary. Provides details like payment_id, amount, etc.

The snapshot view is: `v_report_payments_summary`

## Pie chart configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_payments_summary",
          "reportType": "TABLE",
          "reportPrettyName": "Payments summary",
          "sourceTableName": "report_payments_summary",
          "refreshProcedureName": "refresh_report_payments_summary",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
