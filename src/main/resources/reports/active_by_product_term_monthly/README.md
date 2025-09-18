# Monthly active subscriptions report

Compute (at the end of each month) the total number of active subscriptions per product and billing period.

The snapshot view is: `v_report_active_by_product_term_monthly`

## Timeline configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_active_by_product_term_monthly",
          "reportType": "TIMELINE",
          "reportPrettyName": "Monthly Active Subscriptions by Product Term",
          "sourceTableName": "report_active_by_product_term_monthly",
          "refreshProcedureName": "refresh_active_by_product_term_monthly",
          "refreshFrequency": "DAILY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```
## Report UI:

![report.png](monthly-active-subs-by-product-term.png)