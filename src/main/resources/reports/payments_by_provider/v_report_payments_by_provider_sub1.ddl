create or replace view v_report_payments_by_provider_sub1 as
	SELECT
         a.plugin_name as plugin_name
        ,1 as timeframe        ,a.tenant_record_id
        ,'AUTHORIZE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_auths a
    WHERE 1=1
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,1 as timeframe        ,a.tenant_record_id
        ,'CAPTURE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_captures a
    WHERE 1=1
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,1 as timeframe        ,a.tenant_record_id
        ,'CHARGEBACK' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
         analytics_payment_chargebacks a
    WHERE 1=1
     GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,1 as timeframe        ,a.tenant_record_id
        ,'CREDIT' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
         analytics_payment_credits a
    WHERE 1=1
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,1 as timeframe        ,a.tenant_record_id
        ,'PURCHASE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_purchases a
    WHERE 1=1
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,1 as timeframe        ,a.tenant_record_id
        ,'REFUND' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_refunds a
    WHERE 1=1
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,1 as timeframe        ,a.tenant_record_id
        ,'VOID' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_voids a
    WHERE 1=1
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
    --  ****************************************************************************************************************************
	SELECT
         a.plugin_name as plugin_name
        ,2 as timeframe        ,a.tenant_record_id
        ,'AUTHORIZE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_auths a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 7 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,2 as timeframe        ,a.tenant_record_id
        ,'CAPTURE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_captures a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 7 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,2 as timeframe        ,a.tenant_record_id
        ,'CHARGEBACK' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
         analytics_payment_chargebacks a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 7 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,2 as timeframe        ,a.tenant_record_id
        ,'CREDIT' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
         analytics_payment_credits a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 7 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,2 as timeframe        ,a.tenant_record_id
        ,'PURCHASE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_purchases a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 7 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,2 as timeframe        ,a.tenant_record_id
        ,'REFUND' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_refunds a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 7 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,2 as timeframe        ,a.tenant_record_id
        ,'VOID' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_voids a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 7 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
    --  ****************************************************************************************************************************
	SELECT
         a.plugin_name as plugin_name
        ,3 as timeframe        ,a.tenant_record_id
        ,'AUTHORIZE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_auths a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 1 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,3 as timeframe        ,a.tenant_record_id
        ,'CAPTURE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_captures a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 1 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,3 as timeframe        ,a.tenant_record_id
        ,'CHARGEBACK' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
         analytics_payment_chargebacks a
     WHERE 1=1
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,3 as timeframe        ,a.tenant_record_id
        ,'CREDIT' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
         analytics_payment_credits a
     WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 1 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,3 as timeframe        ,a.tenant_record_id
        ,'PURCHASE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_purchases a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 1 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,3 as timeframe        ,a.tenant_record_id
        ,'REFUND' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_refunds a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 1 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,3 as timeframe        ,a.tenant_record_id
        ,'VOID' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_voids a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 1 day)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
    --  ****************************************************************************************************************************
	SELECT
         a.plugin_name as plugin_name
        ,4 as timeframe        ,a.tenant_record_id
        ,'AUTHORIZE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_auths a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 34 minute)
        AND a.created_date<=date_sub(sysdate(),interval 4 minute)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,4 as timeframe        ,a.tenant_record_id
        ,'CAPTURE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_captures a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 34 minute)
        AND a.created_date<=date_sub(sysdate(),interval 4 minute)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,4 as timeframe        ,a.tenant_record_id
        ,'CHARGEBACK' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
         analytics_payment_chargebacks a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 34 minute)
        AND a.created_date<=date_sub(sysdate(),interval 4 minute)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,4 as timeframe        ,a.tenant_record_id
        ,'CREDIT' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
         analytics_payment_credits a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 34 minute)
        AND a.created_date<=date_sub(sysdate(),interval 4 minute)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,4 as timeframe        ,a.tenant_record_id
        ,'PURCHASE' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_purchases a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 34 minute)
        AND a.created_date<=date_sub(sysdate(),interval 4 minute)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,4 as timeframe        ,a.tenant_record_id
        ,'REFUND' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_refunds a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 34 minute)
        AND a.created_date<=date_sub(sysdate(),interval 4 minute)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
    UNION
	SELECT
         a.plugin_name as plugin_name
        ,4 as timeframe        ,a.tenant_record_id
        ,'VOID' as transaction_type
        ,count(1) as total
        ,sum(case when a.payment_transaction_status in ('UNKNOWN','FAILED','ERRORED') then 1 else 0 end) as failed
        ,sum(case when a.payment_transaction_status = 'PENDING' then 1 else 0 end) as pending
        ,sum(case when a.payment_transaction_status = 'SUCCESS' then 1 else 0 end) as good
    FROM
        analytics_payment_voids a
    WHERE 1=1
        AND a.created_date>date_sub(sysdate(),interval 34 minute)
        AND a.created_date<=date_sub(sysdate(),interval 4 minute)
    GROUP BY
         a.plugin_name
         ,a.tenant_record_id
;