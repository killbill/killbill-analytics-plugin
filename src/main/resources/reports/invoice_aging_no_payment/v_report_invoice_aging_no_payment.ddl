CREATE OR REPLACE VIEW v_report_invoice_aging_no_payment AS
WITH date_buckets AS (
    SELECT
        CURRENT_DATE AS today,
        CURRENT_DATE - INTERVAL 30 DAY AS d_0_30,
        CURRENT_DATE - INTERVAL 60 DAY AS d_30_60,
        CURRENT_DATE - INTERVAL 90 DAY AS d_60_90,
        CURRENT_DATE - INTERVAL 120 DAY AS d_90_120,
        CURRENT_DATE - INTERVAL 150 DAY AS d_120_150
),
invoice_data AS (
    SELECT
        ii.invoice_number,
        ii.account_name,
        ii.account_external_key,
        CAST(ii.created_date AS DATE) AS invoice_creation_date,
        CAST(ii.invoice_date AS DATE) AS invoice_date,
        CAST(ii.start_date AS DATE) AS service_start_date,
        CAST(ii.end_date AS DATE) AS service_end_date,
        ii.bundle_external_key,
        ii.product_name,
        ii.slug,
        ii.currency,
        ii.invoice_original_amount_charged,
        ii.invoice_balance,
        ii.amount,
        ii.created_date,
        ii.invoice_item_record_id,
        ii.tenant_record_id
    FROM analytics_invoice_items ii
    WHERE ii.invoice_date < CAST(DATE_FORMAT(SYSDATE(), '%Y-%m-01') AS DATE)
      AND ii.report_group != 'test'
      and ii.amount > 0
      and ii.invoice_amount_paid=0
      and ii.invoice_amount_charged=ii.invoice_original_amount_charged
)
SELECT
    a.account_name AS "Customer Name",
    a.account_external_key AS "Account Number",
    a.currency AS "Currency",

    -- Balance due buckets
    CASE WHEN a.invoice_creation_date > b.d_0_30 THEN a.invoice_original_amount_charged ELSE 0 END AS "Balance due 0-30 Days",
    CASE WHEN a.invoice_creation_date BETWEEN b.d_30_60 AND b.d_0_30 THEN a.invoice_original_amount_charged ELSE 0 END AS "Balance due 30-60 Days",
    CASE WHEN a.invoice_creation_date BETWEEN b.d_60_90 AND b.d_30_60 THEN a.invoice_original_amount_charged ELSE 0 END AS "Balance due 60-90 Days",
    CASE WHEN a.invoice_creation_date BETWEEN b.d_90_120 AND b.d_60_90 THEN a.invoice_original_amount_charged ELSE 0 END AS "Balance due 90-120 Days",
    CASE WHEN a.invoice_creation_date BETWEEN b.d_120_150 AND b.d_90_120 THEN a.invoice_original_amount_charged ELSE 0 END AS "Balance due 120-150 Days",
    CASE WHEN a.invoice_creation_date < b.d_120_150 THEN a.invoice_original_amount_charged ELSE 0 END AS "Balance due 150+ Days",

    a.invoice_original_amount_charged AS "Total Balance Due",

    -- Balance due in USD
    CASE WHEN a.invoice_creation_date > b.d_0_30 THEN CASE WHEN a.currency != 'USD' THEN ROUND(cc.reference_rate * a.invoice_original_amount_charged, 4) ELSE a.invoice_original_amount_charged END ELSE 0 END AS "Balance due 0-30 Days USD",
    CASE WHEN a.invoice_creation_date BETWEEN b.d_30_60 AND b.d_0_30 THEN CASE WHEN a.currency != 'USD' THEN ROUND(cc.reference_rate * a.invoice_original_amount_charged, 4) ELSE a.invoice_original_amount_charged END ELSE 0 END AS "Balance due 30-60 Days USD",
    CASE WHEN a.invoice_creation_date BETWEEN b.d_60_90 AND b.d_30_60 THEN CASE WHEN a.currency != 'USD' THEN ROUND(cc.reference_rate * a.invoice_original_amount_charged, 4) ELSE a.invoice_original_amount_charged END ELSE 0 END AS "Balance due 60-90 Days USD",
    CASE WHEN a.invoice_creation_date BETWEEN b.d_90_120 AND b.d_60_90 THEN CASE WHEN a.currency != 'USD' THEN ROUND(cc.reference_rate * a.invoice_original_amount_charged, 4) ELSE a.invoice_original_amount_charged END ELSE 0 END AS "Balance due 90-120 Days USD",
    CASE WHEN a.invoice_creation_date BETWEEN b.d_120_150 AND b.d_90_120 THEN CASE WHEN a.currency != 'USD' THEN ROUND(cc.reference_rate * a.invoice_original_amount_charged, 4) ELSE a.invoice_original_amount_charged END ELSE 0 END AS "Balance due 120-150 Days USD",
    CASE WHEN a.invoice_creation_date < b.d_120_150 THEN CASE WHEN a.currency != 'USD' THEN ROUND(cc.reference_rate * a.invoice_original_amount_charged, 4) ELSE a.invoice_original_amount_charged END ELSE 0 END AS "Balance due 150+ Days USD",

    CASE WHEN a.currency != 'USD' THEN cc.reference_rate * a.invoice_original_amount_charged ELSE a.invoice_original_amount_charged END AS "Total Balance Due USD" ,

    a.invoice_number AS "Invoice Number",
    a.bundle_external_key AS "Bundle External Key",
    a.slug AS "Slug",
    a.service_start_date AS "Service Start Date",
    a.service_end_date AS "Service End Date",
    a.invoice_date AS "Invoice Date",
    a.invoice_original_amount_charged AS "Invoice Amount",
    a.invoice_balance AS "Invoice Balance",
    case when a.currency != 'USD' then ROUND(cc.reference_rate * a.invoice_original_amount_charged, 4) else a.invoice_original_amount_charged end AS "Invoice Amount USD",
    case when a.currency != 'USD' then ROUND(cc.reference_rate * a.invoice_balance, 4) else a.invoice_balance end AS "Invoice Balance USD",
    a.tenant_record_id
FROM invoice_data a
LEFT OUTER JOIN analytics_currency_conversion cc
    ON a.created_date >= cc.start_date
   AND a.created_date <= cc.end_date
   AND cc.currency = a.currency
CROSS JOIN date_buckets b
ORDER BY a.account_name, a.invoice_number, a.invoice_item_record_id;