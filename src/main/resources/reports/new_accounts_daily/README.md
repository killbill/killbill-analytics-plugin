# Daily new accounts report

Compute the total amount of new accounts created per day for each tenant.

The snapshot view is: `v_report_new_accounts_daily`

## Timeline configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_new_accounts_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily New Accounts",
          "sourceTableName": "report_new_accounts_daily",
          "refreshProcedureName": "refresh_report_new_accounts_daily",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Report UI:

![daily-new-accounts.png](daily-new-accounts.png)
