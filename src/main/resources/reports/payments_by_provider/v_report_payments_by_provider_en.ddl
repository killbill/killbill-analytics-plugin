create or replace view v_report_payments_by_provider_en as
SELECT
  t1.plugin_name
, t1.tenant_record_id
, t2.timeframe
, t1.transaction_type
, case when t2.timeframe=1 then 'Total'
       when t2.timeframe=2 then 'Last 7 Days'
       when t2.timeframe=3 then 'Last 24 Hours'
       when t2.timeframe=4 then 'Last 30 min'
  end as period
, sum(ifnull(total,0)) as total
, sum(ifnull(failed,0)) as failed
, sum(ifnull(pending,0)) as pending
, sum(ifnull(good,0)) as good
, case when failed is not null and total is not null then concat(round(((sum(failed)/sum(total))*100),2),'%')
       else '0%'
  end as pct_failed
, case when failed is not null and total is not null then concat(round(((sum(pending)/sum(total))*100),2),'%')
       else '0%'
  end as pct_pending
, case when failed is not null and total is not null then concat(round(((sum(good)/sum(total))*100),2),'%')
       else '0%'
  end as pct_good
, date_format(sysdate(), '%Y-%m-%d') as day
, 1 as count
FROM v_report_payments_by_provider_sub2 t1
INNER JOIN v_report_payments_by_provider_sub3 t2
LEFT OUTER JOIN v_report_payments_by_provider_sub1 v1 on v1.plugin_name=t1.plugin_name
AND v1.timeframe=t2.timeframe
AND v1.tenant_record_id=t1.tenant_record_id
GROUP BY
  plugin_name
, timeframe
, transaction_type
, tenant_record_id
ORDER BY
  tenant_record_id
, plugin_name
, timeframe
;
