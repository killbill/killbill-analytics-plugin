# Daily Chargebacks Report

Compute the total value (in the reference currency) of chargebacks per day per currency.

The snapshot view is: `v_report_chargebacks_daily`

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_chargebacks_daily",
          "reportType": "TIMELINE",
          "reportPrettyName": "Daily Chargebacks Value",
          "sourceTableName": "report_chargebacks_daily",
          "refreshProcedureName": "refresh_report_chargebacks_daily",
          "refreshFrequency": "DAILY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
