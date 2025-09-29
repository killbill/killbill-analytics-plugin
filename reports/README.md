# KillBill Analytics Reports Setup Script

This script installs all necessary database DDLs and creates KillBill analytics reports for your KillBill environment.

---

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Usage](#usage)
- [Environment Variables](#environment-variables)
- [Script Behavior](#script-behavior)
- [Examples](#examples)
- [Reports List](#reports_list)


---

## Overview

This Bash script performs the following tasks:

1. Installs database DDL files (`.sql` or `.ddl`) into the configured MySQL database.
2. Creates KillBill analytics reports via the KillBill Analytics plugin REST API.
3. Supports optional dropping of existing reports before creation.

The script recursively installs DDL files, ensuring `v_report_*.ddl` files are installed before corresponding `report_*.ddl` files. If no `v_report_*.ddl` exists in a folder, all `.ddl` files in that folder are installed.

---

## Prerequisites

- **Bash shell** (Linux, macOS, or Windows Git Bash)
- **MySQL client** installed and accessible in PATH
- KillBill server running with the KillBill Analytics plugin installed
- Appropriate permissions for the MySQL database and KillBill API

---

## Usage

Run the script from the directory containing your DDL files:

```bash
./setup_reports.sh
```

By default, the script installs DDLs and creates all reports.

---

## Environment Variables

The script uses environment variables to configure MySQL and KillBill settings. Defaults are provided if variables are not set:

| Variable                 | Default                  | Description                                                               |
|--------------------------|--------------------------|---------------------------------------------------------------------------|
| `KILLBILL_HTTP_PROTOCOL` | `http`                   | KillBill API protocol                                                     |
| `KILLBILL_HOST`          | `127.0.0.1`              | KillBill host                                                             |
| `KILLBILL_PORT`          | `8080`                   | KillBill port                                                             |
| `KILLBILL_USER`          | `admin`                  | KillBill username                                                         |
| `KILLBILL_PASSWORD`      | `password`               | KillBill password                                                         |
| `KILLBILL_API_KEY`       | `bob`                    | KillBill API key                                                          |
| `KILLBILL_API_SECRET`    | `lazar`                  | KillBill API secret                                                       |
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

## Script Behavior

1. **DDL Installation**
    - Installs ddl from the `utils` directory first.
    - Recursively installs DDLs from the other subdirectories:
        - If `v_report_*.ddl` files exist, they are installed first, followed by `report_*.ddl`.
        - If no `v_report_*.ddl` exists, all `.ddl` files in the folder are installed.

2. **Report Creation**
    - All reports defined in the `create_all_reports` function are created.
    - If `DROP_EXISTING_REPORT=true`, existing reports are deleted before creation.
    - Reports are created via the KillBill Analytics plugin REST API.

---

## Examples

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

- **Override KillBill host and MySQL password:**

```bash
export KILLBILL_HOST=192.168.1.10
export MYSQL_PASSWORD=mysecret
./setup_reports.sh
```

## Reports List

The script creates the following reports:

|Report Name| Underlying Report Table               |Report Description|
|--|---------------------------------------|--|
| [accounts_summary](accounts_summary/README.md) | report_accounts_summary               |Provides an account summary. Provides details like account balance, account status, currency, etc.|
| [active_by_product_term_monthly](active_by_product_term_monthly/README.md) | report_active_by_product_term_monthly |Compute (at the end of each month) the total number of active subscriptions per product and billing period.|
| [bundles_summary](bundles_summary/README.md) | report_bundles_summary                |Provides a subscription bundle summary. Provides details like CTD, plan name, price, for the base subscription in a bundle.|
| [cancellations_daily](cancellations_daily/README.md) | report_cancellations_daily            |Compute the total number of cancellations per day per phase.|
| [chargebacks_daily](chargebacks_daily/README.md) | report_chargebacks_daily              |Compute the total value (in the reference currency) of chargebacks per day per currency.|
| [conversion-total-dollar-amount](conversion-total-dollar-amount/README.md) | report_conversion-total-dollar-amount |Compute (monthly) the total revenue from subscriptions converting out of trial, grouped by tenant and billing period.|
| [invoice_aging](invoice_aging/README.md) | report_invoice_aging                  |This report lists all customer invoice aging with remaining balances, breaking them into standard aging buckets and converting amounts to USD for easy comparison.|
| [invoice_aging_no_payment](invoice_aging_no_payment/README.md) | report_invoice_aging_no_payment       |This report lists all customer invoices with no payments recorded, categorizing outstanding amounts into standard aging buckets and converting balances into USD for comparison.
|
| [invoice_credits_daily](invoice_credits_daily/README.md) | report_invoice_credits_daily          |Total of invoice credits per tenant, per currency and per day.|
| [invoice_credits_monthly](invoice_credits_monthly/README.md) | report_invoice_credits_monthly        |Report of all invoice credits from the previous month, showing amounts in both original currency and USD equivalents.|
| [invoice_item_adjustments_daily](invoice_item_adjustments_daily/README.md) | report_invoice_item_adjustments_daily                |Total of  invoice item adjustments per tenant, per currency and per day.|
| [invoice_item_adjustments_monthly](invoice_item_adjustments_monthly/README.md) | report_invoice_item_adjustments_monthly                |Report of all invoice item adjustments from the previous month, showing amounts in both original currency and USD equivalents.|
| [invoice_items_monthly](invoice_items_monthly/README.md) | report_invoice_items_monthly                |Report of all invoice items from the previous month, showing amounts in both original currency and USD equivalents.|
| [invoices_balance_daily](invoices_balance_daily/README.md) | report_invoices_balance_daily                |Compute the total sum of invoices balance (in the reference currency) per invoice created day.
|
| [invoices_daily](invoices_daily/README.md) | report_invoices_daily                |Compute the total invoice amount charged (in the reference currency) per day per currency.|
| [invoices_monthly](invoices_monthly/README.md) | report_invoices_monthly                |Report of all invoices from the previous month, showing amounts in both original currency and USD equivalents.|
| [mrr_daily](mrr_daily/README.md) | report_mrr_daily                |Computes the total active MRR (monthly recurring revenue), broken down both by product and as a tenant-wide total (ALL) for each tenant and each day.|
| [new_accounts_daily](new_accounts_daily/README.md) | report_new_accounts_daily                |Compute the total amount of new accounts created per day for each tenant.|
| [overdue-states-count-daily](overdue-states-count-daily/README.md) | report_overdue-states-count-daily                |Count of overdue states per tenant and per day.|
| [payments_monthly](payments_monthly/README.md) | report_payments_monthly               |Report of all payments from the previous month, showing amounts in both original currency and USD equivalents.|
| [payments_summary](payments_summary/README.md) | report_payments_summary                |Provides payment summary. Provides details like payment_id, amount, etc.|
| [payments_total_daily](payments_total_daily/README.md) | report_payments_total_daily                |Compute the total value (in the reference currency) of payments per day per currency.|
| [refunds-monthly](refunds-monthly/README.md) | report_refunds-monthly                |Report of all refunds from the previous month, showing amounts in both original currency and USD equivalents.|
| [refunds_total_daily](refunds_total_daily/README.md) | report_refunds_total_daily                |Compute the total value (in the reference currency) of refunds per day per currency for each tenant.|
| [subscribers-vs-non-subscribers](subscribers-vs-non-subscribers/README.md) | report_subscribers-vs-non-subscribers                |Compute the total number of active (i.e. with at least one active subscription) and non-active accounts per tenant.|
| [trial-starts-count-daily](trial-starts-count-daily/README.md) | report_trial-starts-count-daily                |Count of new trial subscriptions per tenant, per day and per product.|
| [trial-to-no-trial-conversions_daily](trial-to-no-trial-conversions_daily/README.md) | report_trial-to-no-trial-conversions_daily                |Count of subscriptions converting from trial to non-trial per tenant per day.|

