# Kill Bill Reports Overview

This document provides details about the built-in reports provided by the analytics plugin. It also provides instructions for installing the reports as well as details of the script that can be used to install all the reports.

## Reports Creation

All the built-in reports are present in the [reports](https://github.com/killbill/killbill-analytics-plugin/reports) directory. Within this directory, there are separate directories for each report. 

The following files are present in each report directory:

* `v_report_xxx` - This is the DDL for the view corresponding to the report
* `report_xxx` - This is the DDL corresponding to the database table/stored procedure corresponding to the report
* `README` - This includes the documentation for the report as well as the `curl` command to create the report
* `xxx.png` - This is a screenshot for the report

In order to create a report, you need to do the following:

1. Run the DDL inside the `v_report_xxx` to create the view.
2. Run the DDL inside `report_xxx` to create the stored procedure.
3. Run the `curl` command within the README


## Report Script

In addition, we also provide the [reports_setup.sh](reports_setup.sh) script. This script automatically creates all the reports in the `reports` directory. 

### Prerequisites

- **Bash shell** (Linux, macOS, or Windows Git Bash)
- **MySQL client** installed and accessible in PATH
- Kill Bill running with the Kill Bill Analytics plugin installed
- Appropriate permissions for the MySQL database and Kill Bill API

---

### Usage

Run the script as follows:

```bash
./setup_reports.sh
```

By default, the script installs DDLs and creates all reports.

---

### Environment Variables

The script uses environment variables to configure MySQL and Kill Bill settings. Defaults are provided if variables are not set:

| Variable                 | Default                  | Description                                                               |
|--------------------------|--------------------------|---------------------------------------------------------------------------|
| `KILLBILL_HTTP_PROTOCOL` | `http`                   | Kill Bill API protocol                                                    |
| `KILLBILL_HOST`          | `127.0.0.1`              | Kill Bill host                                                            |
| `KILLBILL_PORT`          | `8080`                   | Kill Bill port                                                            |
| `KILLBILL_USER`          | `admin`                  | Kill Bill username                                                        |
| `KILLBILL_PASSWORD`      | `password`               | Kill Bill password                                                        |
| `KILLBILL_API_KEY`       | `bob`                    | Kill Bill API key                                                         |
| `KILLBILL_API_SECRET`    | `lazar`                  | Kill Bill API secret                                                      |
| `MYSQL_HOST`             | `127.0.0.1`              | MySQL host                                                                |
| `MYSQL_USER`             | `root`                   | MySQL user                                                                |
| `MYSQL_PASSWORD`         | `killbill`               | MySQL password                                                            |
| `MYSQL_DATABASE`         | `killbill`               | MySQL database name                                                       |
| `INSTALL_DDL`            | `true`                   | Whether to install DDL files (`true` or `false`)                          |
| `DROP_EXISTING_REPORT`   | `true`                   | Whether to drop existing reports before creating them (`true` or `false`) |

You can export environment variables before running the script to override defaults:

```bash
export KILLBILL_HOST=192.168.1.10
export MYSQL_PASSWORD=mysecret
export INSTALL_DDL=false

./setup_reports.sh
```

---

### Examples

- **Run with default configuration:**

```bash
./setup_reports.sh
```

- **Skip DDL installation:**

```bash
export INSTALL_DDL=false
./setup_reports.sh
```

- **Disable dropping existing reports:**

```bash
export DROP_EXISTING_REPORT=false
./setup_reports.sh
```

- **Override Kill Bill host and MySQL password:**

```bash
export KILLBILL_HOST=192.168.1.10
export MYSQL_PASSWORD=mysecret
./setup_reports.sh
```

## Reports Overview

The following table provides an overview of all the available reports.

| Report Name                                                                          | Underlying Report Table               | Report Description                                                                                                                                                               |
|--------------------------------------------------------------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [accounts_summary](accounts_summary/README.md)                                       | report_accounts_summary               | Provides an account summary. Provides details like account balance, account status, currency, etc.                                                                               |
| [active_by_product_term_monthly](active_by_product_term_monthly/README.md)           | report_active_by_product_term_monthly | Compute (at the end of each month) the total number of active subscriptions per product and billing period.                                                                      |
| [bundles_summary](bundles_summary/README.md)                                         | report_bundles_summary                | Provides a subscription bundle summary. Provides details like CTD, plan name, price, for the base subscription in a bundle.                                                      |
| [cancellations_daily](cancellations_daily/README.md)                                 | report_cancellations_daily            | Compute the total number of cancellations per day per phase.                                                                                                                     |
| [chargebacks_daily](chargebacks_daily/README.md)                                     | report_chargebacks_daily              | Compute the total value (in the reference currency) of chargebacks per day per currency.                                                                                         |
| [churn](churn/README.md)                                                             | report_churn_percent              | Shows the churn percentage for monthly and annual subscriptions on a per-tenant, per-month basis.                                                                                |
| [churn](churn/README.md)                                                             | report_churn_total_usd              | Shows the total churned revenue for monthly and annual subscriptions on a per-tenant, per-month basis.                                                                           |
| [conversion-total-dollar-amount](conversion-total-dollar-amount/README.md)           | report_conversion-total-dollar-amount | Compute (monthly) the total revenue from subscriptions converting out of trial, grouped by tenant and billing period.                                                            |
| [invoice_aging](invoice_aging/README.md)                                             | report_invoice_aging                  | This report lists all customer invoice aging with remaining balances, breaking them into standard aging buckets and converting amounts to USD for easy comparison.               |
| [invoice_aging_no_payment](invoice_aging_no_payment/README.md)                       | report_invoice_aging_no_payment       | This report lists all customer invoices with no payments recorded, categorizing outstanding amounts into standard aging buckets and converting balances into USD for comparison. 
| [invoice_credits_daily](invoice_credits_daily/README.md)                             | report_invoice_credits_daily          | Total of invoice credits per tenant, per currency and per day.                                                                                                                   |
| [invoice_credits_monthly](invoice_credits_monthly/README.md)                         | report_invoice_credits_monthly        | Report of all invoice credits from the previous month, showing amounts in both original currency and USD equivalents.                                                            |
| [invoice_item_adjustments_daily](invoice_item_adjustments_daily/README.md)           | report_invoice_item_adjustments_daily                | Total of  invoice item adjustments per tenant, per currency and per day.                                                                                                         |
| [invoice_item_adjustments_monthly](invoice_item_adjustments_monthly/README.md)       | report_invoice_item_adjustments_monthly                | Report of all invoice item adjustments from the previous month, showing amounts in both original currency and USD equivalents.                                                   |
| [invoice_items_monthly](invoice_items_monthly/README.md)                             | report_invoice_items_monthly                | Report of all invoice items from the previous month, showing amounts in both original currency and USD equivalents.                                                              |
| [invoices_balance_daily](invoices_balance_daily/README.md)                           | report_invoices_balance_daily                | Compute the total sum of invoices balance (in the reference currency) per invoice created day.                                                                                   
| [invoices_daily](invoices_daily/README.md)                                           | report_invoices_daily                | Compute the total invoice amount charged (in the reference currency) per day per currency.                                                                                       |
| [invoices_monthly](invoices_monthly/README.md)                                       | report_invoices_monthly                | Report of all invoices from the previous month, showing amounts in both original currency and USD equivalents.                                                                   |
| [mrr_daily](mrr_daily/README.md)                                                     | report_mrr_daily                | Computes the total active MRR (monthly recurring revenue), broken down both by product and as a tenant-wide total (ALL) for each tenant and each day.                            |
| [new_accounts_daily](new_accounts_daily/README.md)                                   | report_new_accounts_daily                | Compute the total amount of new accounts created per day for each tenant.                                                                                                        |
| [overdue-states-count-daily](overdue-states-count-daily/README.md)                   | report_overdue-states-count-daily                | Count of overdue states per tenant and per day.                                                                                                                                  |
| [payment_provider_conversions](payment_provider_conversions/README.md)                    | report_payment_provider_conversions               | Compare the total number of transactions and the number of successful transactions that have occurred in a recent 15 minutes period to the same metrics that occurred in the corresponding 15 minutes from 14 days ago.                                                                                                                      |
| [payment_provider_errors](payment_provider_errors/README.md)                         | report_payment_provider_errors               | Compute the top errors per provider and currency, per day.                                                                                                                       |
| [payment_provider_monitor](payment_provider_monitor/README.md)                       | report_payment_provider_monitor               | Compute the number of successful transactions that have occurred in the past hour, for each payment service provider that has had transactions within the last week.             |
| [payments_by_provider](payments_by_provider/README.md)                               | report_payments_by_provider               | Compute the number of payments by transaction state over different timeframes for each payment service provider (plugin).                                                        |
| [payments_by_provider](payments_by_provider/README.md)                               | report_payments_by_provider_last_24h_summary               | Compute the number of payments by transaction state over different timeframes for each payment service provider (plugin).                                                        |
| [payments_monthly](payments_monthly/README.md)                                       | report_payments_monthly               | Report of all payments from the previous month, showing amounts in both original currency and USD equivalents.                                                                   |
| [payments_summary](payments_summary/README.md)                                       | report_payments_summary                | Provides payment summary. Provides details like payment_id, amount, etc.                                                                                                         |
| [payments_total_daily](payments_total_daily/README.md)                               | report_payments_total_daily                | Compute the total value (in the reference currency) of payments per day per currency.                                                                                            |
| [refunds-monthly](refunds-monthly/README.md)                                         | report_refunds-monthly                | Report of all refunds from the previous month, showing amounts in both original currency and USD equivalents.                                                                    |
| [refunds_total_daily](refunds_total_daily/README.md)                                 | report_refunds_total_daily                | Compute the total value (in the reference currency) of refunds per day per currency for each tenant.                                                                             |
| [subscribers-vs-non-subscribers](subscribers-vs-non-subscribers/README.md)           | report_subscribers-vs-non-subscribers                | Compute the total number of active (i.e. with at least one active subscription) and non-active accounts per tenant.                                                              |
| [trial-starts-count-daily](trial-starts-count-daily/README.md)                       | report_trial-starts-count-daily                | Count of new trial subscriptions per tenant, per day and per product.                                                                                                            |
| [trial-to-no-trial-conversions_daily](trial-to-no-trial-conversions_daily/README.md) | report_trial-to-no-trial-conversions_daily                | Count of subscriptions converting from trial to non-trial per tenant per day.                                                                                                    |

