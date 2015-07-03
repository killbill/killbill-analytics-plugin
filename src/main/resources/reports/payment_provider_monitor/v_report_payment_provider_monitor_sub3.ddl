create or replace view v_report_payment_provider_monitor_sub3 as
SELECT
    plugin_name
    ,tenant_record_id
    ,sum(ifnull(cnt,0)) as cnt
FROM v_report_payment_provider_monitor_sub2 t2
GROUP BY
    plugin_name
    ,tenant_record_id
;
