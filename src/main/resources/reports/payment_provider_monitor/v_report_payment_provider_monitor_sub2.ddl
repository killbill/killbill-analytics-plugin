create or replace view v_report_payment_provider_monitor_sub2 as
SELECT
    apc.plugin_name
    ,apc.tenant_record_id
    ,sum(case when apc.created_date > sysdate() - interval 1 hour then 1 else 0 end) success_count_last_hour
    ,count(1) success_count_last_12_hours
FROM
    analytics_payment_captures apc
WHERE 1=1
    AND apc.payment_transaction_status='SUCCESS'
    AND apc.created_date > sysdate() - interval '12' hour
GROUP BY
    apc.plugin_name
    ,apc.tenant_record_id
UNION
SELECT
    app.plugin_name
    ,app.tenant_record_id
    ,sum(case when app.created_date > sysdate() - interval 1 hour then 1 else 0 end) success_count_last_hour
    ,count(1) success_count_last_12_hours
FROM
    analytics_payment_purchases app
WHERE 1=1
    AND app.payment_transaction_status='SUCCESS'
    AND app.created_date > sysdate() - interval '12' hour
GROUP BY
    app.plugin_name
    ,app.tenant_record_id
;
