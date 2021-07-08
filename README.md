# Analytics plugin
![Maven Central](https://img.shields.io/maven-central/v/org.kill-bill.billing.plugin.java/analytics-plugin?color=blue&label=Maven%20Central)

Kill Bill plugin to provide business analytics and reporting capabilities. You can find the documentation [here](http://docs.killbill.io/latest/userguide_analytics.html).

## Kill Bill compatibility

| Plugin version | Kill Bill version |
| -------------: | ----------------: |
| 1.x.y          | 0.14.z            |
| 3.x.y          | 0.16.z            |
| 4.x.y          | 0.18.z            |
| 5.x.y          | 0.19.z            |
| 6.x.y          | 0.20.z            |
| 7.0.y          | 0.22.z            |
| 7.1.y          | 0.22.z            |
| 7.2.y          | 0.22.z            |

### Release notes

* Starting with 7.2.0, the configuration is using a YAML format, instead of key-value pairs.
* We've upgraded numerous dependencies in 7.1.x (required for Java 11 support).

## Requirements

The plugin needs a database. The latest version of the schema can be found [here](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/org/killbill/billing/plugin/analytics/ddl.sql).

## Installation

Locally:

```
kpm install_java_plugin analytics --from-source-file target/analytics-plugin-*-SNAPSHOT.jar --destination /var/tmp/bundles
```

## Configuration

```
curl -v \
     -X POST \
     -u admin:password \
     -H 'X-Killbill-ApiKey: bob' \
     -H 'X-Killbill-ApiSecret: lazar' \
     -H 'X-Killbill-CreatedBy: admin' \
     -H 'Content-Type: text/plain' \
     -d '!!org.killbill.billing.plugin.analytics.api.core.AnalyticsConfiguration
  blacklist:
    - 468e5259-6635-4988-9ae7-3d79b11fc6ed
    - f7da09af-8593-4a88-b6d4-1c4ebf807103
  ignoredGroups:
    - FIELDS
  pluginPropertyKeys:
    killbill-stripe:
      1: processorResponse
      2: avsResultCode
      3: cvvResultCode
  databases:
    warehouse:
      type: trino
      url: jdbc:trino://example.net:8080/hive/sales?user=admin' \
    http://127.0.0.1:8080/1.0/kb/tenants/uploadPluginConfig/killbill-analytics
```

## Setup

Default dashboards rely on reports that need to be installed by running the [seed_reports.sh](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/seed_reports.sh) script.

By default, the views will use the *converted* amount columns when applicable, which require the `analytics_currency_conversion` table to be populated with currency conversion rates. If you are only using one currency, use the non-converted columns instead (`next_mrr` instead of `converted_next_mrr` for example).

When configuring refreshes via stored procedures, make sure to bump the connection timeout accordingly (`org.killbill.billing.osgi.dao.connectionTimeout`), as it will be used to set the read and query timeouts.

## API

### Data

To retrieve all data for a given account:

```
curl -v \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     "http://127.0.0.1:8080/plugins/killbill-analytics/<ACCOUNT_ID>"
```

To force a refresh:

```
curl -v \
     -X PUT \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     "http://127.0.0.1:8080/plugins/killbill-analytics/<ACCOUNT_ID>"
```

To refresh all accounts:

```
curl -s \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     "http://127.0.0.1:8080/1.0/kb/accounts/pagination" | \
ruby -r json -e 'JSON.parse(gets).map { |a| puts a["accountId"] }' | \
xargs -I accountId \
curl -v \
     -X PUT \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
    "http://127.0.0.1:8080/plugins/killbill-analytics/accountId"
```

### Reports

There are 3 report types:

* `COUNTERS`: This is used for pie charts.
  * Local: Each row must have 3 columns (`tenant_record_id`, `label`, and `count`).
  * Remote: Only the columns `label`, and `count` are mandatory. You can optionally specify the placeholder `TENANT_RECORD_ID` in the query which will be replaced by the `tenantRecordId` before the query is executed.
* `TIMELINE`: This is used for line charts.
  * Local: The columns `day` (or `ts`) and `tenant_record_id` are mandatory
  * Remote: The column `day` (or `ts`) is mandatory. You can optionally specify the placeholders `TENANT_RECORD_ID`, `START_DATE`, and `END_DATE` in the query which will be replaced by the `tenantRecordId` and specified dates by the user before the query is executed.
* `TABLE`: This is used for rendering underlying data.
  * Local: Each row must have at least a `tenant_record_id` column.
  * Remote: You can optionally specify the placeholder `TENANT_RECORD_ID` in the query which will be replaced by the `tenantRecordId` before the query is executed.

To create a report based on a local view:

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_accounts_summary",
          "reportType": "COUNTERS",
          "reportPrettyName": "Accounts summary",
          "sourceTableName": "report_accounts_summary",
          "refreshProcedureName": "refresh_report_accounts_summary",
          "refreshFrequency": "HOURLY"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

To create a report based on SQL executed on a remote database (`warehouse`):

```
curl -v \
     -X POST \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     -H 'Content-Type: application/json' \
     -d '{"reportName": "report_historical_orders",
          "reportType": "TABLE",
          "reportPrettyName": "Historical orders",
          "sourceName": "warehouse",
          "sourceQuery": "select * from warehouse.public.orders"}' \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports"
```

To retrieve a report configuration by name:

```
curl -v \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports/report_accounts_summary"
```

To retrieve a report SQL query:

```
curl -v \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports?name=report_accounts_summary&startDate=2018-01-01&endDate=2018-05-01&sqlOnly=true"
```

To retrieve report data:

```
curl -v \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports?name=report_accounts_summary&startDate=2018-01-01&endDate=2018-05-01&smooth=SUM_WEEKLY&format=csv"
```

To delete a report configuration by name:

```
curl -v \
     -X DELETE \
     -u admin:password \
     -H "X-Killbill-ApiKey:bob" \
     -H "X-Killbill-ApiSecret:lazar" \
     "http://127.0.0.1:8080/plugins/killbill-analytics/reports/report_accounts_summary"
```

### Healthcheck

Status:

```
curl -v \
     -u admin:password \
     "http://127.0.0.1:8080/plugins/killbill-analytics/healthcheck"
```

Put out of rotation:

```
curl -v \
     -X DELETE \
     -u admin:password \
     "http://127.0.0.1:8080/plugins/killbill-analytics/healthcheck"
```

Put in rotation:

```
curl -v \
     -X PUT \
     -u admin:password \
     "http://127.0.0.1:8080/plugins/killbill-analytics/healthcheck"
```

## About

Kill Bill is the leading Open-Source Subscription Billing & Payments Platform. For more information about the project, go to https://killbill.io/.
