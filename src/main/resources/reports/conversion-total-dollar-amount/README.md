# Conversions Total Dollar Amount Report

Compute (monthly) the total revenue from subscriptions converting out of trial, grouped by tenant and billing period.

The snapshot view is: `v_report_conversions_total_dollar_monthly`

## Timeline configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_conversions_total_dollar_monthly",
          "reportType": "TIMELINE",
          "reportPrettyName": "Conversions Total Dollar Amount",
          "sourceTableName": "report_conversions_total_dollar_monthly",
          "refreshProcedureName": "refresh_report_conversions_total_dollar_monthly",
          "refreshFrequency": "DAILY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Sample Data

|Tenant Record Id|Day |Billing Period| Count|
|--|--|--|--|
|1|2025-06-01  |WEEKLY |30|
|1|2025-07-01  |MONTHLY |30|
|1| 2025-07-01 |QUARTERLY |70|
|6| 2025-01-01 |ANNUAL|200|
|1| 2025-04-01 |MONTHLY|30|

Here day represents the first day of the month representing that subscription's conversion month. So if the subscription converts from TRIAL to EVERGREEN phase on `2025-04-15`, the day will be `2025-04-01`.

## Report UI:

![conversion-total-dollar-amount.png](conversion-total-dollar-amount.png)
