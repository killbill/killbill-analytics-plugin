# Analytics plugin v2 design

## Introduction

In the early days of Kill Bill, we wanted to provide stable reporting tables while the core system was rapidly evolving; this let us modify the core tables without having to worry about an ETL or a dashboard breaking down the line.

To do so, we decided to copy all the data from Kill Bill into a set of fact tables maintained by the Analytics plugin and used for reporting. Both schemas were independent.

Kill Bill is now mature, and the core tables seldom change: the fear of destabilizing reports is less of a concern nowadays. We now encourage BI teams to build views and ETLs on top of the raw data.

Now, not everything can be accessed through SQL: computing an invoice balance for instance must be done in code. Some core tables are also difficult to query directly (e.g. `subscription_events`). For these use cases, we still need the plugin to capture such information into tables that can be accessed for reporting.

## v2 design

The plugin can be configured to generate fact tables:

* When events are triggered in the system (real-time mode)
* On a periodic schedule (e.g. once an hour or once a day)
* On demand (via API)

## v2 schema

`analytics_accounts_snapshot`:

| Column name | Description |
| ----------: | ----------: |
| `account_id` | Points to the `id` column of the `accounts` table |
| `balance` | Sum of all invoices balance for that account, minus all credits |
| `cba` | Sum of all account credits |
| `currency` | Currency for the amount columns |
| `manual_pay` | Whether the account is marked as `MANUAL_PAY` |
| `auto_pay_off` | Whether the account is marked as `AUTO_PAY_OFF` |
| `auto_invoicing_off` | Whether the account is marked as `AUTO_INVOICING_OFF` |
| `auto_invoicing_draft` | Whether the account is marked as `AUTO_INVOICING_DRAFT` |
| `auto_invoicing_reuse_draft` | Whether the account is marked as `AUTO_INVOICING_REUSE_DRAFT` |
| `overdue_enforcement_off` | Whether the account is marked as `OVERDUE_ENFORCEMENT_OFF` |
| `test` | Whether the account is marked as `TEST` |
| `partner` | Whether the account is marked as `PARTNER` |
| `closed` | Whether the account is closed |
| `cf1_value` | (Optional) Account custom field value 1 (if configured) |
| `cf2_value` | (Optional) Account custom field value 2 (if configured) |
| `cf3_value` | (Optional) Account custom field value 3 (if configured) |
| `created_date` | Audit log creation date |
| `created_by` | Audit log user |
| `created_reason_code` | Audit log reason code |
| `created_comments` | Audit log comments |

`analytics_subscriptions_snapshot`:

| Column name | Description |
| ----------: | ----------: |
| `subscription_id` | Points to the `id` column of the `subscriptions` table |
| `bcd` | The bill cycle day for that subscription in the account timezone |
| `product_name` | The current or last active product name (before cancellation) |
| `product_category` | The current or last active product category (before cancellation) |
| `product_phase_name` | The current or last active phase name (before cancellation) |
| `product_phase_type` | The current or last active phase type (before cancellation) |
| `plan_name` | The current or last active plan name (before cancellation) |
| `pricelist_name` | The current or last pricelist name (before cancellation) |
| `catalog_name` | The catalog name associated with the current or last plan (before cancellation) |
| `catalog_version` | The catalog version associated with the current or last plan (before cancellation) |
| `entitlement_state` | The current entitlement state |
| `entitlement_start_date` | The start date of the entitlement |
| `entitlement_end_date` | The end date of the entitlement, that is the date at which it got cancelled |
| `billing_start_date` | The date at which the billing started for that subscription |
| `billing_end_date` | The date at which the billing stopped for that subscription |
| `cf1_value` | (Optional) Subscription custom field value 1 (if configured) |
| `cf2_value` | (Optional) Subscription custom field value 2 (if configured) |
| `cf3_value` | (Optional) Subscription custom field value 3 (if configured) |
| `created_date` | Audit log creation date |
| `created_by` | Audit log user |
| `created_reason_code` | Audit log reason code |
| `created_comments` | Audit log comments |

`analytics_subscriptions_transitions_snapshot`:

| Column name | Description |
| ----------: | ----------: |
| `subscription_event_record_id` | Points to the `record_id` column of the `subscription_events` table |
| `subscription_id` | Points to the `id` column of the `subscriptions` table |
| `requested_timestamp` | The date when the transition took place |
| `event` | User friendly transition name |
| `service` | The service associated with this transition |
| `prev_bcd` | The bill cycle day for that subscription in the account timezone prior to this transition |
| `prev_product_name` | The product name  prior to this transition |
| `prev_product_category` | The product category prior to this transition |
| `prev_product_phase_name` | The phase name prior to this transition |
| `prev_product_phase_type` | The phase type prior to this transition |
| `prev_plan_name` | The plan name prior to this transition |
| `prev_pricelist_name` | The pricelist prior to this transition |
| `prev_catalog_name` | The catalog name associated with the plan prior to this transition |
| `prev_catalog_version` | The catalog version associated with the plan prior to this transition |
| `prev_entitlement_state` | The entitlement state prior to this transition |
| `prev_entitlement_start_date` | The start date of the entitlement prior to this transition |
| `next_bcd` | The bill cycle day for that subscription in the account timezone after this transition |
| `next_product_name` | The product name  after this transition |
| `next_product_category` | The product category after this transition |
| `next_product_phase_name` | The phase name after this transition |
| `next_product_phase_type` | The phase type after this transition |
| `next_plan_name` | The plan name after this transition |
| `next_pricelist_name` | The pricelist after this transition |
| `next_catalog_name` | The catalog name associated with the plan after this transition |
| `next_catalog_version` | The catalog version associated with the plan after this transition |
| `next_entitlement_state` | The entitlement state after this transition |
| `next_entitlement_end_date` | The end date of the entitlement after this transition |
| `created_date` | Audit log creation date |
| `created_by` | Audit log user |
| `created_reason_code` | Audit log reason code |
| `created_comments` | Audit log comments |

`analytics_invoices_snapshot`:

| Column name | Description |
| ----------: | ----------: |
| `invoice_id` | Points to the `id` column of the `invoices` table |
| `raw_balance` | `charges-credits-adjustments-(payments-refunds)` |
| `balance` | Invoice balance as seen by Kill Bill: DRAFT, VOID, and WRITTEN_OFF invoices, or paid children invoices, have a zero balance regardless of the raw balance |
| `amount_paid` | Sum of all successful payment amounts for that invoice |
| `original_amount_charged` | Sum of all `EXTERNAL_CHARGE`, `FIXED`, and `RECURRING` item amounts when the invoice was created |
| `amount_charged` | Sum of all charges (EXTERNAL_CHARGE, FIXED, RECURRING) and adjustments (item or invoice adjustment) amounts |
| `amount_credit` | Sum of all `CBA_ADJ` items |
| `amount_refunded` | Sum of all refunds and chargebacks for payments associated with that invoice |
| `currency` | Currency for the amount columns |
| `written_off` | Whether the invoice has been marked as written off |
| `cf1_value` | (Optional) Invoice custom field value 1 (if configured) |
| `cf2_value` | (Optional) Invoice custom field value 2 (if configured) |
| `cf3_value` | (Optional) Invoice custom field value 3 (if configured) |
| `created_date` | Audit log creation date |
| `created_by` | Audit log user |
| `created_reason_code` | Audit log reason code |
| `created_comments` | Audit log comments |
