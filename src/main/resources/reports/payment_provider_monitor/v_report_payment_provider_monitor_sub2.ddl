create or replace view v_report_payment_provider_monitor_sub2 as
SELECT
    apc.plugin_name
    ,apc.tenant_record_id
    ,count(1) cnt
FROM
    analytics_payment_captures apc
WHERE 1=1
    AND apc.payment_transaction_status='SUCCESS'
    AND apc.created_date > sysdate() - interval 1 hour
GROUP BY
    apc.plugin_name
    ,apc.tenant_record_id
UNION
SELECT
    app.plugin_name
    ,app.tenant_record_id
    ,count(1) cnt
FROM
    analytics_payment_purchases app
WHERE 1=1
    AND app.payment_transaction_status='SUCCESS'
    AND app.created_date > sysdate() - interval 1 hour
GROUP BY
    app.plugin_name
    ,app.tenant_record_id
;
