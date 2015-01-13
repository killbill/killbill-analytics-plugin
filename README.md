Analytics plugin
================

Kill Bill plugin to provide business analytics and reporting capabilities. You can find the documentation [here](https://github.com/killbill/killbill-docs/blob/v2/userguide/subscription/analytics-overview.adoc).

Release builds are available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.kill-bill.billing.plugin.java%22%20AND%20a%3A%22analytics-plugin%22) with coordinates `org.kill-bill.billing.plugin.java:analytics-plugin`.


Setup
-----

Default dashboards rely on reports that need to be installed by running the [seed_reports.sh](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/seed_reports.sh) script.


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
