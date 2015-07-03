create or replace view v_report_payment_provider_monitor as
SELECT
    plugin_list.plugin_name
    ,ifnull(recent_success_trx.cnt,0) as SUCCESS_COUNT_LAST_HOUR
    ,'1970-01-01' as day
    ,1 as count
    ,plugin_list.tenant_record_id
FROM
    v_report_payment_provider_monitor_sub1 plugin_list
    LEFT OUTER JOIN v_report_payment_provider_monitor_sub3 recent_success_trx on
        plugin_list.plugin_name=recent_success_trx.plugin_name
        AND plugin_list.tenant_record_id=recent_success_trx.tenant_record_id
;
