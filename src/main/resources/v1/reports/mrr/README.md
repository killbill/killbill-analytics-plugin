# Daily MRR

Compute the Monthly Recurring Revenue (MRR) on a daily basis.

The snapshot view is: `v_report_mrr_daily`

## Timeline configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_mrr_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily MRR",
          "sourceTableName": "report_mrr_daily",
          "refreshProcedureName": "refresh_report_mrr_daily",
          "refreshFrequency": "DAILY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
