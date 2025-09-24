# Payments Monthly Report

Report of all payments from the previous month, showing amounts in both original currency and USD equivalents.

The snapshot view is: `v_report_payments_monthly`

## Prerequisites

This report requires the `analytics_currency_conversion` table to be populated. See [insertMonthlyCurrencyConversionRates.ddl](../utils/insertMonthlyCurrencyConversionRates.ddl)

## Report Configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_payments_monthly",
          "reportType": "TABLE",
          "reportPrettyName": "Payments Monthly Report",
          "sourceTableName": "report_payments_monthly",
          "refreshProcedureName": "refresh_report_payments_monthly",
          "refreshFrequency": "DAILY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Report UI:

![payments-monthly.png](payments-monthly.png)
