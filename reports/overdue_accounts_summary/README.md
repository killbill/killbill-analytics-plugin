# Overdue Accounts Summary Report

Breakdown of current vs. overdue accounts by tenant.

The snapshot view is: [v_report_overdue_account_summary](v_report_overdue_account_summary.ddl)

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_overdue_accounts_summary",
          "reportType": "COUNTERS",
          "reportPrettyName": "Overdue Accounts Summary",
          "sourceTableName": "report_overdue_accounts_summary",
          "refreshProcedureName": "refresh_report_overdue_accounts_summary",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Sample Data

| Tenant Record Id | Label   | Count |
|------------------|---------|-------|
| 515              | Overdue | 5     |
| 518              | Overdue | 1     |
| 1                | Overdue | 74    |
| 1                | Current | 23    |
| 256              | Overdue | 3     |


The first row in the above table indicates that the tenant with record id=1 had 5 accounts in the `Overdue` state.

## Report UI:

![overdue-accounts-summary.png](overdue-accounts-summary.png)



