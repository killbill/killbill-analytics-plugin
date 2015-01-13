select
  'BAC' as table_name
, count(1) count
from analytics_accounts bac
left outer join accounts a on bac.account_id = a.id
where 1 = 1
and (
     bac.account_record_id != a.record_id
  or bac.account_record_id is null
)
union
select
  'BAC_FIELDS' as table_name
, count(1) count
from analytics_account_fields bac
left outer join accounts a on bac.account_id = a.id
where 1 = 1
and (
     bac.account_record_id != a.record_id
  or a.record_id is null
)
union
select
  'BAC_TAGS' as table_name
, count(1) count
from analytics_account_tags bac
left outer join accounts a on bac.account_id = a.id
where 1 = 1
and (
     bac.account_record_id != a.record_id
  or bac.account_record_id is null
)
union
select
  'BBU' as table_name
, count(1) count
from analytics_bundles bbu
left outer join accounts a on bbu.account_id = a.id
where 1 = 1
and (
     bbu.account_record_id != a.record_id
  or bbu.account_record_id is null
)
union
select
  'BBU_FIELDS' as table_name
, count(1) count
from analytics_bundle_fields bbu
left outer join accounts a on bbu.account_id = a.id
where 1 = 1
and (
     bbu.account_record_id != a.record_id
  or bbu.account_record_id is null
)
union
select
  'BBU_TAGS' as table_name
, count(1) count
from analytics_bundle_tags bbu
left outer join accounts a on bbu.account_id = a.id
where 1 = 1
and (
     bbu.account_record_id != a.record_id
  or bbu.account_record_id is null
)
union
select
  'BII' as table_name
, count(1) count
from analytics_invoice_items bii
left outer join accounts a on bii.account_id = a.id
where 1 = 1
and (
     bii.account_record_id != a.record_id
  or bii.account_record_id is null
)
union
select
  'BIA' as table_name
, count(1) count
from analytics_invoice_adjustments bia
left outer join accounts a on bia.account_id = a.id
where 1 = 1
and (
     bia.account_record_id != a.record_id
  or bia.account_record_id is null
)
union
select
  'BIIA' as table_name
, count(1) count
from analytics_invoice_item_adjustments biia
left outer join accounts a on biia.account_id = a.id
where 1 = 1
and (
     biia.account_record_id != a.record_id
  or biia.account_record_id is null
)
union
select
  'BIC' as table_name
, count(1) count
from analytics_invoice_credits bic
left outer join accounts a on bic.account_id = a.id
where 1 = 1
and (
     bic.account_record_id != a.record_id
  or bic.account_record_id is null
)
union
select
  'BIN' as table_name
, count(1) count
from analytics_invoices bin
left outer join accounts a on bin.account_id = a.id
where 1 = 1
and (
     bin.account_record_id != a.record_id
  or bin.account_record_id is null
)
union
select
  'BIN_FIELDS' as table_name
, count(1) count
from analytics_invoice_fields bin
left outer join accounts a on bin.account_id = a.id
where 1 = 1
and (
     bin.account_record_id != a.record_id
  or bin.account_record_id is null
)
union
select
  'BIN_TAGS' as table_name
, count(1) count
from analytics_invoice_tags bin
left outer join accounts a on bin.account_id = a.id
where 1 = 1
and (
     bin.account_record_id != a.record_id
  or bin.account_record_id is null
)
union
select
  'BIP_AUTHS' as table_name
, count(1) count
from analytics_payment_auths bip
left outer join accounts a on bip.account_id = a.id
where 1 = 1
and (
     bip.account_record_id != a.record_id
  or bip.account_record_id is null
)
union
select
  'BIP_CAPTURES' as table_name
, count(1) count
from analytics_payment_captures bip
left outer join accounts a on bip.account_id = a.id
where 1 = 1
and (
     bip.account_record_id != a.record_id
  or bip.account_record_id is null
)
union
select
  'BIP_PURCHASES' as table_name
, count(1) count
from analytics_payment_purchases bip
left outer join accounts a on bip.account_id = a.id
where 1 = 1
and (
     bip.account_record_id != a.record_id
  or bip.account_record_id is null
)
union
select
  'BIP_REFUNDS' as table_name
, count(1) count
from analytics_payment_refunds bip
left outer join accounts a on bip.account_id = a.id
where 1 = 1
and (
     bip.account_record_id != a.record_id
  or bip.account_record_id is null
)
union
select
  'BIP_CREDITS' as table_name
, count(1) count
from analytics_payment_credits bip
left outer join accounts a on bip.account_id = a.id
where 1 = 1
and (
     bip.account_record_id != a.record_id
  or bip.account_record_id is null
)
union
select
  'BIP_CHARGEBACKS' as table_name
, count(1) count
from analytics_payment_chargebacks bip
left outer join accounts a on bip.account_id = a.id
where 1 = 1
and (
     bip.account_record_id != a.record_id
  or bip.account_record_id is null
)
union
select
  'BIP_FIELDS' as table_name
, count(1) count
from analytics_payment_fields bip
left outer join accounts a on bip.account_id = a.id
where 1 = 1
and (
     bip.account_record_id != a.record_id
  or bip.account_record_id is null
)
union
select
  'BIP_TAGS' as table_name
, count(1) count
from analytics_payment_tags bip
left outer join accounts a on bip.account_id = a.id
where 1 = 1
and (
     bip.account_record_id != a.record_id
  or bip.account_record_id is null
)
union
select
  'BOS' as table_name
, count(1) count
from analytics_account_transitions bos
left outer join accounts a on bos.account_id = a.id
where 1 = 1
and (
     bos.account_record_id != a.record_id
  or bos.account_record_id is null
)
union
select
  'BST' as table_name
, count(1) count
from analytics_subscription_transitions bst
left outer join accounts a on bst.account_id = a.id
where 1 = 1
and (
     bst.account_record_id != a.record_id
  or bst.account_record_id is null
)
;
