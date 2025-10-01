# Refunds Monthly Report

Report of all refunds from the previous month, showing amounts in both original currency and USD equivalents.

The snapshot view is: `v_report_refunds_monthly`

## Prerequisites

This report requires the `analytics_currency_conversion` table to be populated. See [insertMonthlyCurrencyConversionRates.ddl](../utils/insertMonthlyCurrencyConversionRates.ddl)

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_refunds_monthly",
          "reportType": "TABLE",
          "reportPrettyName": "Refunds Monthly Report",
          "sourceTableName": "report_refunds_monthly",
          "refreshProcedureName": "refresh_report_refunds_monthly",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Report UI:

![refunds-monthly.png](refunds-monthly.png)
