create or replace view v_report_payments_by_provider_es as
SELECT
    t1.plugin_name
    ,t2.timeframe
    ,case when t2.timeframe=1 then 'Último mes'
          when t2.timeframe=2 then 'Últ. semana'
          when t2.timeframe=3 then 'Últimas 24 Hrs.'
          when t2.timeframe=4 then 'Últ. 30 min'
     end as period
    ,sum(ifnull(total,0)) as total
    ,sum(ifnull(failed,0)) as failed
    ,sum(ifnull(pending,0)) as pending
    ,sum(ifnull(good,0)) as good
    ,case when failed is not null and total is not null then concat(round(((sum(failed)/sum(total))*100),2),'%')
          else '0%'
     end as pct_failed
    ,case when failed is not null and total is not null then concat(round(((sum(pending)/sum(total))*100),2),'%')
         else '0%'
     end as pct_pending
    ,case when failed is not null and total is not null then concat(round(((sum(good)/sum(total))*100),2),'%')
         else '0%'
     end as pct_good
    ,'1970-01-01' as day
    ,1 as count
    ,t1.tenant_record_id
FROM
    v_report_payments_by_provider_sub2 t1
    INNER JOIN v_report_payments_by_provider_sub3 t2
    LEFT OUTER JOIN v_report_payments_by_provider_sub1 v1 on
        v1.plugin_name=t1.plugin_name
        AND v1.timeframe=t2.timeframe
        AND v1.tenant_record_id=t1.tenant_record_id
GROUP BY
    plugin_name
    ,timeframe
    ,tenant_record_id
ORDER BY    tenant_record_id
    ,plugin_name
    ,timeframe
;
