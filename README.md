Analytics plugin
================

Kill Bill plugin to provide business analytics and reporting capabilities. You can find the documentation [here](https://github.com/killbill/killbill-docs/blob/v2/userguide/subscription/analytics-overview.adoc).

Release builds are available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.kill-bill.billing.plugin.java%22%20AND%20a%3A%22analytics-plugin%22) with coordinates `org.kill-bill.billing.plugin.java:analytics-plugin`.

Kill Bill compatibility
-----------------------

| Plugin version | Kill Bill version |
| -------------: | ----------------: |
| 1.x.y          | 0.14.z            |
| 3.x.y          | 0.16.z            |
| 4.x.y          | 0.18.z            |

Requirements
------------

The plugin needs a database. The latest version of the schema can be found [here](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/org/killbill/billing/plugin/analytics/ddl.sql).

Setup
-----

Default dashboards rely on reports that need to be installed by running the [seed_reports.sh](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/seed_reports.sh) script.

By default, the views will use the *converted* amount columns when applicable, which require the `analytics_currency_conversion` table to be populated with currency conversion rates. If you are only using one currency, use the non-converted columns instead (`next_mrr` instead of `converted_next_mrr` for example).

When configuring refreshes via stored procedures, make sure to bump the connection timeout accordingly (`org.killbill.billing.osgi.dao.connectionTimeout`), as it will be used to set the read and query timeouts.

API
---

To retrieve all data for a given account:

```
curl -u admin:password \
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
