select 'CyberSource sanity' as sanity_query_name;
select
   'payment_transactions in cybersource_transactions' as description
  ,sum(case when b.kb_payment_transaction_id is null then 1 else 0 end) as row_missing
  ,sum(case when b.kb_payment_transaction_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.transaction_type!='VOID' then 
            case when a.id=b.kb_payment_transaction_id and a.payment_id=b.kb_payment_id and a.transaction_type=b.transaction_type and a.amount*100=b.amount_in_cents and a.currency=b.currency and a.payment_id=b.kb_payment_id and a.transaction_type=b.transaction_type and p.account_id=b.kb_account_id
                 then 1 else 0 end
            when a.transaction_type='VOID' then
            case when a.id=b.kb_payment_transaction_id and a.payment_id=b.kb_payment_id and a.transaction_type=b.transaction_type and p.account_id=b.kb_account_id
                 then 1 else 0 end
        end) as matches
  ,count(1) total
from
     payment_transactions a
     inner join payments p on
         a.payment_id=p.id
	 inner join payment_methods pm on
         p.payment_method_id=pm.id
         and pm.plugin_name='killbill-cybersource'
     left outer join cybersource_transactions b on
         a.id=b.kb_payment_transaction_id
where 1=1
     and a.transaction_status='SUCCESS'
UNION
select
   'cybersource_transactions in payment_transactions'
  ,sum(case when b.id is null then 1 else 0 end) as row_missing
  ,sum(case when b.id is not null then 1 else 0 end) as row_exists
  ,count(1) matches
  ,count(1) total
from
     cybersource_transactions a
     left outer join payment_transactions b on
         a.kb_payment_transaction_id=b.id
UNION
select
   'payment_responses in cybersource_transactions' as description
  ,sum(case when b.kb_payment_transaction_id is null then 1 else 0 end) as row_missing
  ,sum(case when b.kb_payment_transaction_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.kb_payment_transaction_id and a.payment_id=b.kb_payment_id and a.transaction_type=b.transaction_type and p.account_id=b.kb_account_id then 1 else 0 end) as matches
  ,count(1) total
from
     payment_transactions a
     inner join payments p on
         a.payment_id=p.id
	 inner join payment_methods pm on
         p.payment_method_id=pm.id
         and pm.plugin_name='killbill-cybersource'
     left outer join cybersource_responses b on
         a.id=b.kb_payment_transaction_id
where 1=1
     and a.transaction_status='SUCCESS'
UNION
select
   'cybersource_responses in payment_transactions'
  ,sum(case when b.id is null then 1 else 0 end) as row_missing
  ,sum(case when b.id is not null then 1 else 0 end) as row_exists
  ,count(1) matches
  ,count(1) total
from
     cybersource_responses a
     left outer join payment_transactions b on
         a.kb_payment_transaction_id=b.id     
where 1=1
    and a.api_call !='add_payment_method'
UNION
select
   'cybersource_responses in cybersource_transactions'
  ,sum(case when b.id is null then 1 else 0 end) as row_missing
  ,sum(case when b.id is not null then 1 else 0 end) as row_exists
  ,count(1) matches
  ,count(1) total
from 
    cybersource_transactions a
    left outer join cybersource_responses b on
         a.cybersource_response_id = b.id
;