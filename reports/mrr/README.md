# Daily MRR

Computes the total active MRR (monthly recurring revenue), broken down both by product and as a tenant-wide total (ALL) for each tenant and each day.

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

## Sample Data

| tenant_record_id | product | day        | count  |
| ---------------- | ------- | ---------- |--------|
| 24               | Pistol  | 2025-01-05 | 150.00 |
| 24               | Rifle   | 2025-01-05 | 200.00 |
| 24               | ALL     | 2025-01-05 | 350.00 |
| 24               | Pistol  | 2025-01-06 | 150.00 |
| 24               | ALL     | 2025-01-06 | 150.00 |


## Report UI:

![daily-mrr.png](daily-mrr.png)