# Bundles Summary Report

Provides a subscription bundle summary. Provides details like CTD, plan name, price, for the base subscription in a bundle.

The snapshot view is: `v_report_bundles_summary`

## Pie chart configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_bundles_summary",
          "reportType": "TABLE",
          "reportPrettyName": "Bundles summary",
          "sourceTableName": "report_bundles_summary",
          "refreshProcedureName": "refresh_report_bundles_summary",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
