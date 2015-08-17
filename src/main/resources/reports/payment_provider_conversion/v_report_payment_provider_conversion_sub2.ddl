create or replace view v_report_payment_provider_conversion_sub2 as
SELECT
    apc.plugin_name
    ,apc.tenant_record_id
    ,sum(case when apc.payment_transaction_status='SUCCESS' then 1
              else 0 end) as historical_success_count
    ,count(1) as historical_transaction_count
FROM
    analytics_payment_captures apc
WHERE 1=1
    AND apc.created_date < FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60))
    AND TIME(FROM_UNIXTIME(UNIX_TIMESTAMP(apc.created_date)- UNIX_TIMESTAMP(apc.created_date)%(15*60))) = TIME(FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60)))
    AND apc.created_date >= sysdate() - interval 14 day
GROUP BY
    apc.plugin_name
    ,apc.tenant_record_id
UNION
SELECT
    app.plugin_name
    ,app.tenant_record_id
    ,sum(case when app.payment_transaction_status='SUCCESS' then 1
              else 0 end) as historical_success_count
    ,count(1) as historical_transaction_count
FROM
    analytics_payment_purchases app
WHERE 1=1
    AND app.created_date < FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60))
    AND TIME(FROM_UNIXTIME(UNIX_TIMESTAMP(app.created_date)- UNIX_TIMESTAMP(app.created_date)%(15*60))) = TIME(FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - 15*60 - (UNIX_TIMESTAMP(NOW()) - 15*60)%(15*60)))
    AND app.created_date >= sysdate() - interval 14 day
GROUP BY
    app.plugin_name
    ,app.tenant_record_id
;