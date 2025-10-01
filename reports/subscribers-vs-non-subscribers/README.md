# Subscribers vs Non-subscribers Report

Compute the total number of active (i.e. with at least one active subscription) and non-active accounts per tenant.

The snapshot view is: `v_report_subscribers_vs_non_subscribers`

## Report Creation

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_subscribers_vs_non_subscribers",
          "reportType": "COUNTERS",
          "reportPrettyName": "Subscribers vs Non Subscribers",
          "sourceTableName": "report_subscribers_vs_non_subscribers",
          "refreshProcedureName": "refresh_report_subscribers_vs_non_subscribers",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

## Sample Data

| ID  | Type           | Count |
|-----|----------------|-------|
| 1   | Non-subscriber | 74    |
| 1   | Subscriber     | 18    |
| 2   | Subscriber     | 1     |
| 5   | Non-subscriber | 1     |
| 5   | Subscriber     | 3     |

The first row in the above table indicates that the tenant with record id=1 had 74 non-subscribers (accounts with no active subscriptions).


## Report UI:

![subscribers-vs-non-subscribers.png](subscribers-vs-non-subscribers.png)


