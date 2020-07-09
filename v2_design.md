# Analytics plugin v2 design

## Introduction

In the early days of Kill Bill, we wanted to provide stable reporting tables while the core system was rapidly evolving; this let us modify the core tables without having to worry about an ETL or a dashboard breaking down the line.

To do so, we decided to copy all of the data from Kill Bill into a set of fact tables maintained by the Analytics plugin and used for reporting. Both schemas were independent.

Kill Bill is now mature and the core tables seldom change: the fear of destabilizing reports is less of a concern nowadays. We now encourage BI teams to build views and ETLs on top of the raw data.

Now, not everything can be accessed through SQL: computing an invoice balance for instance must be done in code. Some core tables are also difficult to query directly (e.g. `subscription_events`). For these usecases, we still need the plugin to capture such information into tables that can be accessed for reporting.

## v2 design

The plugin can be configured to generate fact tables:

* When events are triggered in the system (real-time mode)
* On a periodic schedule (e.g. once an hour or once a day)
* On demand (via API)

## v2 schema

`analytics_accounts_snapshot`:

| Column name | Description |
| ----------: | ----------: |
| `account_id` | Points to the `id` column of the `accounts` table  |
| `balance` | Sum of all invoices balance for that account, minus all credits  |
| `cba` | Sum of all account credits |
| `manual_pay` | Whether the account is marked as `MANUAL_PAY` |
| `auto_pay_off` | Whether the account is marked as `AUTO_PAY_OFF` |
| `auto_invoicing_off` | Whether the account is marked as `AUTO_INVOICING_OFF` |
| `auto_invoicing_draft` | Whether the account is marked as `AUTO_INVOICING_DRAFT` |
| `auto_invoicing_reuse_draft` | Whether the account is marked as `AUTO_INVOICING_REUSE_DRAFT` |
| `overdue_enforcement_off` | Whether the account is marked as `OVERDUE_ENFORCEMENT_OFF` |
| `test` | Whether the account is marked as `TEST` |
| `partner` | Whether the account is marked as `PARTNER` |
| `closed` | Whether the account is closed |

`analytics_subscriptions_snapshot`:

| Column name | Description |
| ----------: | ----------: |
| `subscription_id` | Points to the `id` column of the `subscriptions` table  |
| `bcd` | The bill cycle day for that subscription in the account timezone |
| `product_name` | The current or last active product name (before cancellation) |
| `product_type` | The current or last active product type (before cancellation) |
| `product_category` | The current or last active product category (before cancellation) |
| `product_phase_name` | The current or last active phase name (before cancellation) |
| `product_phase_type` | The current or last active phase type (before cancellation) |
| `state` | The current entitlement state |
| `entitlement_start_date` | The start date of the entitlement |
| `entitlement_end_date` | The end date of the entitlement, that is the date at which it got cancelled |
| `billing_start_date` | The date at which the billing started for that subscription |
| `billing_end_date` | The date at which the billing stopped for that subscription |

`analytics_invoices_snapshot`:

| Column name | Description |
| ----------: | ----------: |
| `invoice_id` | Points to the `id` column of the `invoices` table  |
| `raw_balance` | `charges-credits-adjustments-(payments-refunds)` |
| `balance` | Invoice balance as seen by Kill Bill: DRAFT, VOID, and WRITTEN_OFF invoices, or paid children invoices, have a zero balance regardless of the raw balance |
| `amount_paid` | Sum of all successful payment amounts for that invoice |
| `original_amount_charged` | Sum of all `EXTERNAL_CHARGE`, `FIXED`, and `RECURRING` item amounts when the invoice was created |
| `amount_charged` | Sum of all charges (EXTERNAL_CHARGE, FIXED, RECURRING) and adjustments (item or invoice adjustment) amounts |
| `amount_credit` | Sum of all `CBA_ADJ` items |
| `amount_refunded` | Sum of all refunds and chargebacks for payments associated with that invoice |
| `currency` | Currency for the amount columns |
| `written_off` | Whether the invoice has been marked as written off |
