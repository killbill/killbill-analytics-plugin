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

### Per tenant

The analytics plugin allows some optional tenant level configuration. This can be done by executing the following API endpoint:

```
curl -v \
     -X POST \
     -u admin:password \
     -H 'X-Killbill-ApiKey: bob' \
     -H 'X-Killbill-ApiSecret: lazar' \
     -H 'X-Killbill-CreatedBy: admin' \
     -H 'Content-Type: text/plain' \
     -d '!!org.killbill.billing.plugin.analytics.api.core.AnalyticsConfiguration
  refreshDelaySeconds: 10
  lockAttemptRetries: 100
  rescheduleIntervalOnLockSeconds: 10
  enablePartialRefreshes: true
  blacklist:
    - 468e5259-6635-4988-9ae7-3d79b11fc6ed
    - f7da09af-8593-4a88-b6d4-1c4ebf807103
  highCardinalityAccounts:
    - a8e594e5-1b78-4c2d-876b-f09ec36c611c
    - 31ea22c7-19ae-4316-a432-5e6319e49f97
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

### Global

You can also configure the following properties related to the analytics plugin in the [Kill Bill Configuration File](https://docs.killbill.io/latest/userguide_configuration.html#global_configuration_properties):

```
org.killbill.notificationq.analytics.tableName=analytics_notifications
org.killbill.notificationq.analytics.historyTableName=analytics_notifications_history
org.killbill.analytics.lockSleepMilliSeconds=100
```

See [Plugin Configuration](https://docs.killbill.io/latest/userguide_analytics.html#_plugin_configuration) for further information. 

## Setup

The analytics plugin includes a set of canned reports which can be installed by running the [seed_reports.sh](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/seed_reports.sh) script. See [Installing Canned Reports](https://docs.killbill.io/latest/userguide_analytics.html#installing_canned_reports) for further information. By default, the views will use the *converted* amount columns when applicable, which require the `analytics_currency_conversion` table to be populated with currency conversion rates as explained [here](https://docs.killbill.io/latest/userguide_analytics.html#currency_conversion). If you are only using one currency, use the non-converted columns instead (`next_mrr` instead of `converted_next_mrr` for example).

In addition, you can also create custom reports based on database tables/views. 
To create a report based on a local view named `report_accounts_summary` execute the following endpoint:

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

See [Creating Custom Reports](https://docs.killbill.io/latest/userguide_analytics.html#create_custom_reports) for further information. 

When configuring refreshes via stored procedures, make sure to bump the connection timeout accordingly (`org.killbill.billing.osgi.dao.connectionTimeout`), as it will be used to set the read and query timeouts.

In addition to creating reports, you can also perform several other [report operations](https://docs.killbill.io/latest/userguide_analytics.html#_other_report_operations). 

## About

Kill Bill is the leading Open-Source Subscription Billing & Payments Platform. For more information about the project, go to https://killbill.io/.
