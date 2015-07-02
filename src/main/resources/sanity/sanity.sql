-- ACCOUNTS

select 'A1a' as sanity_query_name;
select distinct a.record_id
from accounts a
left outer join analytics_accounts bac on a.id = bac.account_id
where a.record_id != bac.account_record_id
or (coalesce(a.id, '') != coalesce(bac.account_id, ''))
or a.external_key != bac.account_external_key
or (coalesce(a.email, '') != coalesce(bac.email, ''))
or (coalesce(a.name, '') != coalesce(bac.account_name, ''))
or (coalesce(a.first_name_length, '') != coalesce(bac.first_name_length, ''))
or (coalesce(a.currency, '') != coalesce(bac.currency, ''))
or (coalesce(a.billing_cycle_day_local, '') != coalesce(bac.billing_cycle_day_local, ''))
or (coalesce(a.payment_method_id, '') != coalesce(bac.payment_method_id, ''))
or (coalesce(a.time_zone, '') != coalesce(bac.time_zone, ''))
or (coalesce(a.locale, '') != coalesce(bac.locale, ''))
or (coalesce(a.address1, '') != coalesce(bac.address1, ''))
or (coalesce(a.address2, '') != coalesce(bac.address2, ''))
or (coalesce(a.company_name, '') != coalesce(bac.company_name, ''))
or (coalesce(a.city, '') != coalesce(bac.city, ''))
or (coalesce(a.state_or_province, '') != coalesce(bac.state_or_province, ''))
or (coalesce(a.country, '') != coalesce(bac.country, ''))
or (coalesce(a.postal_code, '') != coalesce(bac.postal_code, ''))
or (coalesce(a.phone, '') != coalesce(bac.phone, ''))
or (coalesce(a.migrated, '') != coalesce(bac.migrated, ''))
or (coalesce(a.is_notified_for_invoices, '') != coalesce(bac.notified_for_invoices, ''))
or a.created_date  != bac.created_date
or a.updated_date != bac.updated_date
or a.tenant_record_id != bac.tenant_record_id
;

select 'A1b' as sanity_query_name;
select distinct bac.account_record_id
from analytics_accounts bac
left outer join accounts a on a.id = bac.account_id
where a.record_id != bac.account_record_id
or (coalesce(a.id, '') != coalesce(bac.account_id, ''))
or a.external_key  != bac.account_external_key
or (coalesce(a.email, '') != coalesce(bac.email, ''))
or (coalesce(a.name, '') != coalesce(bac.account_name, ''))
or (coalesce(a.first_name_length, '') != coalesce(bac.first_name_length, ''))
or (coalesce(a.currency, '') != coalesce(bac.currency, ''))
or (coalesce(a.billing_cycle_day_local, '') != coalesce(bac.billing_cycle_day_local, ''))
or (coalesce(a.payment_method_id, '') != coalesce(bac.payment_method_id, ''))
or (coalesce(a.time_zone, '') != coalesce(bac.time_zone, ''))
or (coalesce(a.locale, '') != coalesce(bac.locale, ''))
or (coalesce(a.address1, '') != coalesce(bac.address1, ''))
or (coalesce(a.address2, '') != coalesce(bac.address2, ''))
or (coalesce(a.company_name, '') != coalesce(bac.company_name, ''))
or (coalesce(a.city, '') != coalesce(bac.city, ''))
or (coalesce(a.state_or_province, '') != coalesce(bac.state_or_province, ''))
or (coalesce(a.country, '') != coalesce(bac.country, ''))
or (coalesce(a.postal_code, '') != coalesce(bac.postal_code, ''))
or (coalesce(a.phone, '') != coalesce(bac.phone, ''))
or (coalesce(a.migrated, '') != coalesce(bac.migrated, ''))
or (coalesce(a.is_notified_for_invoices, '') != coalesce(bac.notified_for_invoices, ''))
or (coalesce(a.created_date, '') != coalesce(bac.created_date, ''))
or (coalesce(a.updated_date, '') != coalesce(bac.updated_date, ''))
or (coalesce(a.tenant_record_id, '') != coalesce(bac.tenant_record_id, ''))
;

select 'A2' as sanity_query_name;
select distinct b.account_record_id
from analytics_accounts b
join account_history ah on b.account_record_id = ah.target_record_id
join audit_log al on ah.record_id = al.target_record_id and al.change_type = 'INSERT' and al.table_name = 'ACCOUNT_HISTORY'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- ACCOUNT FIELDS

select 'K1a' as sanity_query_name;
select distinct cf.account_record_id
from custom_fields cf
left outer join analytics_account_fields b on cf.record_id = b.custom_field_record_id and cf.object_id = b.account_id /* To use the index */
where 1 = 1
and (
     coalesce(b.name, 'NULL') != coalesce(cf.field_name, 'NULL')
  or coalesce(b.value, 'NULL') != coalesce(cf.field_value, 'NULL')
  or coalesce(b.created_date, 'NULL') != coalesce(cf.created_date, 'NULL')
  or coalesce(b.account_record_id, 'NULL') != coalesce(cf.account_record_id, 'NULL')
  or coalesce(b.tenant_record_id, 'NULL') != coalesce(cf.tenant_record_id, 'NULL')
)
and cf.object_type = 'ACCOUNT'
;

select 'K1b' as sanity_query_name;
select distinct b.account_record_id
from analytics_account_fields b
left outer join custom_fields cf on cf.record_id = b.custom_field_record_id and cf.object_id = b.account_id /* To use the index */
where 1 = 1
and (
     coalesce(b.name, 'NULL') != coalesce(cf.field_name, 'NULL')
  or coalesce(b.value, 'NULL') != coalesce(cf.field_value, 'NULL')
  or coalesce(b.created_date, 'NULL') != coalesce(cf.created_date, 'NULL')
  or coalesce(b.account_record_id, 'NULL') != coalesce(cf.account_record_id, 'NULL')
  or coalesce(b.tenant_record_id, 'NULL') != coalesce(cf.tenant_record_id, 'NULL')
  or cf.object_type != 'ACCOUNT'
)
;

select 'K2' as sanity_query_name;
select distinct b.account_record_id
from analytics_account_fields b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'K3' as sanity_query_name;
select distinct b.account_record_id
from analytics_account_fields b
join custom_field_history cfh on b.custom_field_record_id = cfh.target_record_id
join audit_log al on cfh.record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'CUSTOM_FIELD_HISTORY'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- ACCOUNT TAGS

select 'L1a' as sanity_query_name;
select distinct t.account_record_id
from tags t
join tag_definitions td on t.tag_definition_id = td.id
left outer join analytics_account_tags b on t.record_id = b.tag_record_id and t.object_id = b.account_id /* To use the index */
where 1 = 1
and (
     coalesce(b.tag_record_id, 'NULL') != coalesce(t.record_id, 'NULL')
  or coalesce(b.name, 'NULL') != coalesce(td.name, 'NULL')
  or coalesce(b.created_date, 'NULL') != coalesce(t.created_date, 'NULL')
  or coalesce(b.account_record_id, 'NULL') != coalesce(t.account_record_id, 'NULL')
  or coalesce(b.tenant_record_id, 'NULL') != coalesce(t.tenant_record_id, 'NULL')
)
and t.object_type = 'ACCOUNT'
;

select 'L1b' as sanity_query_name;
select distinct b.account_record_id
from analytics_account_tags b
left outer join tags t on t.record_id = b.tag_record_id
left outer join tag_definitions td on t.tag_definition_id = td.id
where (coalesce(b.tag_record_id, 'NULL') != coalesce(t.record_id, 'NULL')
or coalesce(b.name, 'NULL') != coalesce(td.name, 'NULL')
or coalesce(b.created_date, 'NULL') != coalesce(t.created_date, 'NULL')
or coalesce(b.account_record_id, 'NULL') != coalesce(t.account_record_id, 'NULL')
or coalesce(b.tenant_record_id, 'NULL') != coalesce(t.tenant_record_id, 'NULL'))
and t.object_type = 'ACCOUNT'
-- Ignore system tags
and t.tag_definition_id not in ('00000000-0000-0000-0000-000000000001',
                                '00000000-0000-0000-0000-000000000002',
                                '00000000-0000-0000-0000-000000000003',
                                '00000000-0000-0000-0000-000000000004',
                                '00000000-0000-0000-0000-000000000005',
                                '00000000-0000-0000-0000-000000000006',
                                '00000000-0000-0000-0000-000000000007')
;

select 'L2' as sanity_query_name;
select distinct b.account_record_id
from analytics_account_tags b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'L3' as sanity_query_name;
select distinct b.account_record_id
from analytics_account_tags b
join tag_history th on b.tag_record_id = th.target_record_id
join audit_log al on th.record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'TAG_HISTORY'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- INVOICE ADJUSTMENTS

select 'B1a' as sanity_query_name;
-- this will find things it thinks should be in bia but it's correct that they're not there
select distinct b.account_record_id
from invoice_items ii
left outer join analytics_invoice_adjustments b on ii.id = b.item_id
where ii.type in ('CREDIT_ADJ','REFUND_ADJ')
and (coalesce(ii.record_id, '') != coalesce(b.invoice_item_record_id, '')
or (coalesce(ii.id, '') != coalesce(b.item_id, ''))
or (coalesce(ii.type, '') != coalesce(b.item_type, ''))
or (coalesce(ii.invoice_id, '') != coalesce(b.invoice_id, ''))
or (coalesce(ii.account_id, '')!= coalesce(b.account_id, ''))
or (coalesce(ii.phase_name, '') != coalesce(b.slug, ''))
or (coalesce(ii.start_date, '') != coalesce(b.start_date, ''))
or (coalesce(ii.amount, '') != coalesce(b.amount, ''))
or (coalesce(ii.currency, '') != coalesce(b.currency, ''))
or (coalesce(ii.linked_item_id, '') != coalesce(b.linked_item_id, ''))
or (coalesce(ii.created_date, '') != coalesce(b.created_date, ''))
or (coalesce(ii.account_record_id, '') != coalesce(b.account_record_id, ''))
or (coalesce(ii.tenant_record_id, '') != coalesce(b.tenant_record_id, '')))
;

select 'B1b' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_adjustments b
left outer join invoice_items ii on ii.id = b.item_id
where (coalesce(ii.record_id, '') != coalesce(b.invoice_item_record_id, ''))
or (coalesce(ii.id, '') != coalesce(b.item_id, ''))
or (coalesce(ii.type, '') != coalesce(b.item_type, ''))
or (coalesce(ii.invoice_id, '') != coalesce(b.invoice_id, ''))
or (coalesce(ii.account_id, '')!= coalesce(b.account_id, ''))
or (coalesce(ii.phase_name, '') != coalesce(b.slug, ''))
or (coalesce(ii.start_date, '') != coalesce(b.start_date, ''))
or (coalesce(ii.amount, '') != coalesce(b.amount, ''))
or (coalesce(ii.currency, '') != coalesce(b.currency, ''))
or (coalesce(ii.linked_item_id, '') != coalesce(b.linked_item_id, ''))
or (coalesce(ii.created_date, '') != coalesce(b.created_date, ''))
or (coalesce(ii.account_record_id, '') != coalesce(b.account_record_id, ''))
or (coalesce(ii.tenant_record_id, '') != coalesce(b.tenant_record_id, ''))
or ii.type not in ('CREDIT_ADJ','REFUND_ADJ')
;

select 'B2' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_adjustments b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'B3' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_adjustments b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

select 'B4' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_adjustments b
left outer join bundles bndl on b.bundle_id = bndl.id
where coalesce(bndl.external_key, 'NULL') != coalesce(b.bundle_external_key, 'NULL')
;

select 'B5' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_adjustments b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

select 'B6' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_adjustments b
join audit_log al on b.invoice_item_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_ITEMS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- INVOICE ITEMS

select 'C1a' as sanity_query_name;
select distinct ii.account_record_id
from invoice_items ii
left outer join analytics_invoice_items bii on ii.id = bii.item_id
where ii.type in ('FIXED','RECURRING','EXTERNAL_CHARGE')
and (coalesce(ii.record_id, '') != coalesce(bii.invoice_item_record_id, '')
or (coalesce(ii.id, '') != coalesce(bii.item_id, ''))
or (coalesce(ii.type, '') != coalesce(bii.item_type, ''))
or (coalesce(ii.invoice_id, '') != coalesce(bii.invoice_id, ''))
or (coalesce(ii.account_id, '') != coalesce(bii.account_id, ''))
or (coalesce(ii.phase_name, '') != coalesce(bii.slug, ''))
or (coalesce(ii.start_date, '') != coalesce(bii.start_date, ''))
or (coalesce(ii.amount, '') != coalesce(bii.amount, ''))
or (coalesce(ii.currency, '') != coalesce(bii.currency, ''))
or (coalesce(ii.linked_item_id, '') != coalesce(bii.linked_item_id, ''))
or (coalesce(ii.created_date, '') != coalesce(bii.created_date, ''))
or (coalesce(ii.account_record_id, '') != coalesce(bii.account_record_id, ''))
or (coalesce(ii.tenant_record_id, '') != coalesce(bii.tenant_record_id, '')))
;

select 'C1b' as sanity_query_name;
select distinct bii.account_record_id
from analytics_invoice_items bii
left outer join invoice_items ii on ii.id = bii.item_id
where (coalesce(ii.record_id, '') != coalesce(bii.invoice_item_record_id, ''))
or (coalesce(ii.id, '') != coalesce(bii.item_id, ''))
or (coalesce(ii.type, '') != coalesce(bii.item_type, ''))
or (coalesce(ii.invoice_id, '') != coalesce(bii.invoice_id, ''))
or (coalesce(ii.account_id, '')!= coalesce(bii.account_id, ''))
or (coalesce(ii.phase_name, '') != coalesce(bii.slug, ''))
or (coalesce(ii.start_date, '') != coalesce(bii.start_date, ''))
or (coalesce(ii.amount, '') != coalesce(bii.amount, ''))
or (coalesce(ii.currency, '') != coalesce(bii.currency, ''))
or (coalesce(ii.linked_item_id, '') != coalesce(bii.linked_item_id, ''))
or (coalesce(ii.created_date, '') != coalesce(bii.created_date, ''))
or (coalesce(ii.account_record_id, '') != coalesce(bii.account_record_id, ''))
or (coalesce(ii.tenant_record_id, '') != coalesce(bii.tenant_record_id, ''))
or ii.type not in ('FIXED','RECURRING','EXTERNAL_CHARGE')
;

select 'C2' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_items b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'C3' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_items b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

select 'C4' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_items b
left outer join bundles bndl on b.bundle_id = bndl.id
where coalesce(bndl.external_key, 'NULL') != coalesce(b.bundle_external_key, 'NULL')
;

select 'C5' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_items b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

select 'C6' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_items b
join audit_log al on b.invoice_item_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_ITEMS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- INVOICE ITEM ADJUSTMENTS

select 'D1a' as sanity_query_name;
select distinct ii.account_record_id
from invoice_items ii
left outer join analytics_invoice_item_adjustments b on ii.id = b.item_id
where ii.type in ('ITEM_ADJ', 'REPAIR_ADJ')
and (coalesce(ii.record_id, '') != coalesce(b.invoice_item_record_id, '')
or (coalesce(ii.id, '') != coalesce(b.item_id, ''))
or (coalesce(ii.type, '') != coalesce(b.item_type, ''))
or (coalesce(ii.invoice_id, '') != coalesce(b.invoice_id, ''))
or (coalesce(ii.account_id, '')!= coalesce(b.account_id, ''))
or ((coalesce(ii.phase_name, '') != coalesce(b.slug,'')) and ii.phase_name is not null)
or (coalesce(ii.start_date, '') != coalesce(b.start_date, ''))
or (coalesce(ii.amount, '') != coalesce(b.amount, ''))
or (coalesce(ii.currency, '') != coalesce(b.currency, ''))
or (coalesce(ii.linked_item_id, '') != coalesce(b.linked_item_id, ''))
or (coalesce(ii.created_date, '') != coalesce(b.created_date, ''))
or (coalesce(ii.account_record_id, '') != coalesce(b.account_record_id, ''))
or (coalesce(ii.tenant_record_id, '') != coalesce(b.tenant_record_id, '')))
;

select 'D1b' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_item_adjustments b
left outer join invoice_items ii on ii.id = b.item_id
where coalesce(ii.record_id, '') != coalesce(b.invoice_item_record_id, '')
or (coalesce(ii.id, '') != coalesce(b.item_id, ''))
or (coalesce(ii.type, '') != coalesce(b.item_type, ''))
or (coalesce(ii.invoice_id, '') != coalesce(b.invoice_id, ''))
or (coalesce(ii.account_id, '')!= coalesce(b.account_id, ''))
/* The code is smart and will populate NULL columns from the linked item id */
or (ii.phase_name is not null and ii.phase_name != b.slug)
or (coalesce(ii.start_date, '') != coalesce(b.start_date, ''))
or ( (coalesce(ii.amount, '') != coalesce(b.amount, '')) and ii.type != 'REPAIR_ADJ' ) -- need to calc correct amount in case of REPAIR_ADJ case
or (coalesce(ii.currency, '') != coalesce(b.currency, ''))
or (coalesce(ii.linked_item_id, '') != coalesce(b.linked_item_id, ''))
or (coalesce(ii.created_date, '') != coalesce(b.created_date, ''))
or (coalesce(ii.account_record_id, '') != coalesce(b.account_record_id, ''))
or (coalesce(ii.tenant_record_id, '') != coalesce(b.tenant_record_id, ''))
or ii.type not in ('ITEM_ADJ','REPAIR_ADJ')
;

select 'D2' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_item_adjustments b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'D3' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_item_adjustments b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

select 'D4' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_item_adjustments b
left outer join bundles bndl on b.bundle_id = bndl.id
where coalesce(bndl.external_key, 'NULL') != coalesce(b.bundle_external_key, 'NULL')
and b.bundle_id is not null
;

select 'D5' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_item_adjustments b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

select 'D6' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_item_adjustments b
join audit_log al on b.invoice_item_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_ITEMS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- INVOICE CREDITS

select 'E1a' as sanity_query_name;
select distinct ii.account_record_id
from invoice_items ii
left outer join analytics_invoice_credits b on ii.id = b.item_id
where ii.type in ('CBA_ADJ')
and (coalesce(ii.record_id, '') != coalesce(b.invoice_item_record_id, '')
or (coalesce(ii.id, '') != coalesce(b.item_id, ''))
or (coalesce(ii.type, '') != coalesce(b.item_type, ''))
or (coalesce(ii.invoice_id, '') != coalesce(b.invoice_id, ''))
or (coalesce(ii.account_id, '')!= coalesce(b.account_id, ''))
or (coalesce(ii.phase_name, '') != coalesce(b.slug, ''))
or (coalesce(ii.start_date, '') != coalesce(b.start_date, ''))
or (coalesce(ii.amount, '') != coalesce(b.amount, ''))
or (coalesce(ii.currency, '') != coalesce(b.currency, ''))
or (coalesce(ii.linked_item_id, '') != coalesce(b.linked_item_id, ''))
or (coalesce(ii.created_date, '') != coalesce(b.created_date, ''))
or (coalesce(ii.account_record_id, '') != coalesce(b.account_record_id, ''))
or (coalesce(ii.tenant_record_id, '') != coalesce(b.tenant_record_id, '')))
;

select 'E1b' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_credits b
left outer join invoice_items ii on ii.id = b.item_id
where (coalesce(ii.record_id, '') != coalesce(b.invoice_item_record_id, ''))
or (coalesce(ii.id, '') != coalesce(b.item_id, ''))
or (coalesce(ii.type, '') != coalesce(b.item_type, ''))
or (coalesce(ii.invoice_id, '') != coalesce(b.invoice_id, ''))
or (coalesce(ii.account_id, '')!= coalesce(b.account_id, ''))
or (coalesce(ii.phase_name, '') != coalesce(b.slug, ''))
or (coalesce(ii.start_date, '') != coalesce(b.start_date, ''))
or (coalesce(ii.amount, '') != coalesce(b.amount, ''))
or (coalesce(ii.currency, '') != coalesce(b.currency, ''))
or (coalesce(ii.linked_item_id, '') != coalesce(b.linked_item_id, ''))
or (coalesce(ii.created_date, '') != coalesce(b.created_date, ''))
or (coalesce(ii.account_record_id, '') != coalesce(b.account_record_id, ''))
or (coalesce(ii.tenant_record_id, '') != coalesce(b.tenant_record_id, ''))
or ii.type not in ('CBA_ADJ')
;

select 'E2' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_credits b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'E3' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_credits b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

select 'E4' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_credits b
left outer join bundles bndl on b.bundle_id = bndl.id
where coalesce(bndl.external_key, 'NULL') != coalesce(b.bundle_external_key, 'NULL')
;

select 'E5' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_credits b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

select 'E6' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoice_credits b
join audit_log al on b.invoice_item_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_ITEMS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- INVOICE FIELDS
/* table not currently used */


-- INVOICE TAGS
/* table not currently used */


-- INVOICES

select 'F1a' as sanity_query_name;
select distinct i.account_record_id
from invoices i
left outer join analytics_invoices bin on i.id = bin.invoice_id
where coalesce(i.record_id, '') != coalesce(bin.invoice_record_id, '')
or coalesce(i.record_id, '') != coalesce(bin.invoice_number, '')
or coalesce(i.id, '') != coalesce(bin.invoice_id, '')
or (coalesce(i.account_id, '') != coalesce(bin.account_id, ''))
or (coalesce(i.invoice_date, '') != coalesce(bin.invoice_date, ''))
or (coalesce(i.target_date, '') != coalesce(bin.target_date, ''))
or (coalesce(i.currency, '') != coalesce(bin.currency, ''))
or (coalesce(i.created_date, '') != coalesce( bin.created_date, ''))
or (coalesce(i.account_record_id, '') != coalesce(bin.account_record_id, ''))
or (coalesce(i.tenant_record_id, '') != coalesce(bin.tenant_record_id, ''))
;

select 'F1b' as sanity_query_name;
select distinct bin.account_record_id
from analytics_invoices bin
left outer join invoices i on i.id = bin.invoice_id
where (coalesce(i.record_id, '') != coalesce(bin.invoice_record_id, ''))
or (coalesce(i.id, '') != coalesce(bin.invoice_id, ''))
or (coalesce(i.account_id, '') != coalesce(bin.account_id, ''))
or (coalesce(i.invoice_date, '') != coalesce(bin.invoice_date, ''))
or (coalesce(i.target_date, '') != coalesce(bin.target_date, ''))
or (coalesce(i.currency, '') != coalesce(bin.currency, ''))
or (coalesce(i.created_date, '') != coalesce(bin.created_date, ''))
or (coalesce(i.account_record_id, '') != coalesce(bin.account_record_id, ''))
;

select 'F2' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoices b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'F3a' as sanity_query_name;
select *
from (
  select
    invoice_id
  , invoice_amount_charged
  , sum(coalesce(amount,0)) bii_sum
  from analytics_invoice_items
  group by invoice_id, invoice_amount_charged, invoice_original_amount_charged
) bii_sum
left outer join (
  select
    invoice_id
  , sum(coalesce(amount,0)) bia_sum
  from analytics_invoice_adjustments
  group by invoice_id
) bia_sum using (invoice_id)
left outer join (
  select
    invoice_id
  , sum(coalesce(amount,0)) biia_sum
  from analytics_invoice_item_adjustments
  group by invoice_id
) biia_sum using (invoice_id)
where bii_sum + coalesce(bia_sum,0) + coalesce(biia_sum,0) != bii_sum.invoice_amount_charged
;

select 'F3b' as sanity_query_name;
select
  bin.invoice_id
, bin.original_amount_charged
, sum(bii.amount)
from analytics_invoice_items bii
join analytics_invoices bin on bii.invoice_id = bin.invoice_id and bii.created_date = bin.created_date
group by bin.invoice_id, bin.original_amount_charged
having sum(bii.amount) != bin.original_amount_charged
;

select 'F3c' as sanity_query_name;
select *
from (
  select
    invoice_id
  , invoice_amount_credited
  , sum(coalesce(amount,0)) biic_sum
  from analytics_invoice_credits biic
  group by invoice_id,invoice_amount_credited
) biic_sum
where biic_sum != biic_sum.invoice_amount_credited
;

select 'F3d' as sanity_query_name;
select
  *
, bip_sum invoice_amount_paid
from (
  select
    invoice_id
  , invoice_amount_paid
  , sum(coalesce(amount,0)) bip_sum
  from analytics_payment_purchases bip
  group by invoice_id, invoice_amount_paid
) bip_sum
where bip_sum != bip_sum.invoice_amount_paid
;

select 'F3e' as sanity_query_name;
select *
from (
  select
    invoice_id
  , balance
  , amount_charged
  , amount_credited
  , amount_paid
  , amount_refunded
  , original_amount_charged
  from analytics_invoices
) bin
left outer join (
  select
    invoice_id
  , sum(coalesce(amount,0)) bipc_sum
  from analytics_payment_chargebacks bipc
  group by invoice_id
) bipc_sum using (invoice_id)
left outer join (
  select
    invoice_id
  , sum(coalesce(amount,0)) bipr_sum
  from analytics_payment_refunds bipr
  group by invoice_id
) bipr_sum using (invoice_id)
where bipc_sum + bipr_sum != bin.amount_refunded
;

select 'F3f' as sanity_query_name;
select distinct bin.account_record_id
from analytics_invoices bin
where bin.amount_charged + bin.amount_credited - bin.amount_paid - bin.amount_refunded != bin.balance
and (bin.amount_charged != 0 and bin.amount_paid != 0 and bin.amount_refunded != 0 and bin.balance != 0) -- deal w / acct credit
;


select 'F6' as sanity_query_name;
select distinct b.account_record_id
from analytics_invoices b
join audit_log al on b.invoice_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICES'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- PAYMENTS

select 'G1a' as sanity_query_name;
select distinct ip.account_record_id
from invoice_payments ip
left outer join analytics_payment_purchases bip on ip.id = bip.invoice_payment_id
where (coalesce(ip.record_id, 'NULL') != coalesce(bip.invoice_payment_record_id, 'NULL')
or coalesce(ip.ID, 'NULL') != coalesce(bip.invoice_payment_id, 'NULL')
or coalesce(ip.invoice_id, 'NULL') != coalesce(bip.invoice_id, 'NULL')
or coalesce(ip.type, 'NULL') != coalesce(bip.invoice_payment_type, 'NULL')
or coalesce(ip.linked_invoice_payment_id, 'NULL') != coalesce(bip.linked_invoice_payment_id, 'NULL')
or coalesce(ip.amount, 'NULL') != coalesce(bip.amount, 'NULL')
or coalesce(ip.currency, 'NULL') != coalesce(bip.currency, 'NULL')
or coalesce(ip.created_date, 'NULL') != coalesce(bip.created_date, 'NULL')
or coalesce(ip.account_record_id, 'NULL') != coalesce(bip.account_record_id, 'NULL')
or coalesce(ip.tenant_record_id, 'NULL') != coalesce(bip.tenant_record_id, 'NULL'))
and ip.type = 'ATTEMPT'
;

select 'G1b' as sanity_query_name;
select distinct bip.account_record_id
from analytics_payment_purchases bip
left outer join invoice_payments ip on ip.id = bip.invoice_payment_id
where (coalesce(ip.record_id, 'NULL') != coalesce(bip.invoice_payment_record_id, 'NULL')
or coalesce(ip.ID, 'NULL') != coalesce(bip.invoice_payment_id, 'NULL')
or coalesce(ip.invoice_id, 'NULL') != coalesce(bip.invoice_id, 'NULL')
or coalesce(ip.type, 'NULL') != coalesce(bip.invoice_payment_type, 'NULL')
or coalesce(ip.linked_invoice_payment_id, 'NULL') != coalesce(bip.linked_invoice_payment_id, 'NULL')
or coalesce(ip.amount, 'NULL') != coalesce(bip.amount, 'NULL')
or coalesce(ip.currency, 'NULL') != coalesce(bip.currency, 'NULL')
or coalesce(ip.created_date, 'NULL') != coalesce(bip.created_date, 'NULL')
or coalesce(ip.account_record_id, 'NULL') != coalesce(bip.account_record_id, 'NULL')
or coalesce(ip.tenant_record_id, 'NULL') != coalesce(bip.tenant_record_id, 'NULL')
or bip.invoice_payment_type != 'ATTEMPT')
and bip.invoice_payment_record_id !=0
;

select 'G2' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_purchases b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'G3' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_purchases b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

select 'G4' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_purchases b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

select 'G5' as sanity_query_name;
select distinct bip.account_record_id
from analytics_payment_purchases bip
left outer join invoice_payments ip on bip.invoice_payment_id = ip.id
left outer join payments p on ip.payment_id = p.id
where coalesce(p.record_id, 'NULL') != coalesce(bip.payment_number, 'NULL')
and bip.invoice_payment_record_id!=0
;

select 'G8' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_purchases b
join audit_log al on b.invoice_payment_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_PAYMENTS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- PAYMENT FIELDS
/* table not currently used */


-- PAYMENT TAGS
/* table not currently used */


-- CHARGEBACKS

select 'H1a' as sanity_query_name;
select distinct ip.account_record_id
from invoice_payments ip
left outer join analytics_payment_chargebacks bipc on ip.id = bipc.invoice_payment_id
where (coalesce(ip.record_id, 'NULL') != coalesce(bipc.invoice_payment_record_id, 'NULL')
or coalesce(ip.ID, 'NULL') != coalesce(bipc.invoice_payment_id, 'NULL')
or coalesce(ip.invoice_id, 'NULL') != coalesce(bipc.invoice_id, 'NULL')
or coalesce(ip.type, 'NULL') != coalesce(bipc.invoice_payment_type, 'NULL')
or coalesce(ip.linked_invoice_payment_id, 'NULL') != coalesce(bipc.linked_invoice_payment_id, 'NULL')
or coalesce(ip.amount, 'NULL') != coalesce(bipc.amount, 'NULL')
or coalesce(ip.currency, 'NULL') != coalesce(bipc.currency, 'NULL')
or coalesce(ip.created_date, 'NULL') != coalesce(bipc.created_date, 'NULL')
or coalesce(ip.account_record_id, 'NULL') != coalesce(bipc.account_record_id, 'NULL')
or coalesce(ip.tenant_record_id, 'NULL') != coalesce(bipc.tenant_record_id, 'NULL'))
and ip.type = 'CHARGED_BACK'
;

select 'H1b' as sanity_query_name;
select distinct bipc.account_record_id
from analytics_payment_chargebacks bipc
left outer join invoice_payments ip on ip.id = bipc.invoice_payment_id
where (coalesce(ip.record_id, 'NULL') != coalesce(bipc.invoice_payment_record_id, 'NULL')
or coalesce(ip.ID, 'NULL') != coalesce(bipc.invoice_payment_id, 'NULL')
or coalesce(ip.invoice_id, 'NULL') != coalesce(bipc.invoice_id, 'NULL')
or coalesce(ip.type, 'NULL') != coalesce(bipc.invoice_payment_type, 'NULL')
or coalesce(ip.linked_invoice_payment_id, 'NULL') != coalesce(bipc.linked_invoice_payment_id, 'NULL')
or coalesce(ip.amount, 'NULL') != coalesce(bipc.amount, 'NULL')
or coalesce(ip.currency, 'NULL') != coalesce(bipc.currency, 'NULL')
or coalesce(ip.created_date, 'NULL') != coalesce(bipc.created_date, 'NULL')
or coalesce(ip.account_record_id, 'NULL') != coalesce(bipc.account_record_id, 'NULL')
or coalesce(ip.tenant_record_id, 'NULL') != coalesce(bipc.tenant_record_id, 'NULL')
or bipc.invoice_payment_type != 'CHARGED_BACK')
and bipc.invoice_payment_record_id!=0
;

select 'H2' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_chargebacks b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'H3' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_chargebacks b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

select 'H4' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_chargebacks b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

select 'H5' as sanity_query_name;
select distinct bipc.account_record_id
from analytics_payment_chargebacks bipc
left outer join invoice_payments ip on bipc.invoice_payment_id = ip.id
left outer join payments p on ip.payment_id = p.id
where coalesce(p.record_id, 'NULL') != coalesce(bipc.payment_number, 'NULL')
and bipc.invoice_payment_record_id!=0
;

select 'H8' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_chargebacks b
join audit_log al on b.invoice_payment_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_PAYMENTS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- REFUNDS

select 'H1a' as sanity_query_name;
select distinct ip.account_record_id
from invoice_payments ip
left outer join analytics_payment_refunds bipr on ip.id = bipr.invoice_payment_id
where (coalesce(ip.record_id, 'NULL') != coalesce(bipr.invoice_payment_record_id, 'NULL')
or coalesce(ip.ID, 'NULL') != coalesce(bipr.invoice_payment_id, 'NULL')
or coalesce(ip.invoice_id, 'NULL') != coalesce(bipr.invoice_id, 'NULL')
or coalesce(ip.type, 'NULL') != coalesce(bipr.invoice_payment_type, 'NULL')
or coalesce(ip.linked_invoice_payment_id, 'NULL') != coalesce(bipr.linked_invoice_payment_id, 'NULL')
or coalesce(ip.amount, 'NULL') != coalesce(bipr.amount, 'NULL')
or coalesce(ip.currency, 'NULL') != coalesce(bipr.currency, 'NULL')
or coalesce(ip.created_date, 'NULL') != coalesce(bipr.created_date, 'NULL')
or coalesce(ip.account_record_id, 'NULL') != coalesce(bipr.account_record_id, 'NULL')
or coalesce(ip.tenant_record_id, 'NULL') != coalesce(bipr.tenant_record_id, 'NULL'))
and ip.type = 'REFUND'
;

select 'H1b' as sanity_query_name;
select distinct bipr.account_record_id
from analytics_payment_refunds bipr
left outer join invoice_payments ip on ip.id = bipr.invoice_payment_id
where (coalesce(ip.record_id, 'NULL') != coalesce(bipr.invoice_payment_record_id, 'NULL')
or coalesce(ip.id, 'NULL') != coalesce(bipr.invoice_payment_id, 'NULL')
or coalesce(ip.invoice_id, 'NULL') != coalesce(bipr.invoice_id, 'NULL')
or coalesce(ip.type, 'NULL') != coalesce(bipr.invoice_payment_type, 'NULL')
or coalesce(ip.linked_invoice_payment_id, 'NULL') != coalesce(bipr.linked_invoice_payment_id, 'NULL')
or coalesce(ip.amount, 'NULL') != coalesce(bipr.amount, 'NULL')
or coalesce(ip.currency, 'NULL') != coalesce(bipr.currency, 'NULL')
or coalesce(ip.created_date, 'NULL') != coalesce(bipr.created_date, 'NULL')
or coalesce(ip.account_record_id, 'NULL') != coalesce(bipr.account_record_id, 'NULL')
or coalesce(ip.tenant_record_id, 'NULL') != coalesce(bipr.tenant_record_id, 'NULL')
or bipr.invoice_payment_type != 'REFUND')
and bipr.invoice_payment_record_id!=0
;

select 'H2' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_refunds b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'H3' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_refunds b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

select 'H4' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_refunds b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

select 'H5' as sanity_query_name;
select distinct bipr.account_record_id
from analytics_payment_refunds bipr
left outer join invoice_payments ip on bipr.invoice_payment_id = ip.id
left outer join payments p on ip.payment_id = p.id
where coalesce(p.record_id, 'NULL') != coalesce(bipr.payment_number, 'NULL')
and bipr.invoice_payment_record_id!=0
;

select 'H8' as sanity_query_name;
select distinct b.account_record_id
from analytics_payment_refunds b
join audit_log al on b.invoice_payment_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_PAYMENTS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- ACCOUNT TRANSITIONS

select 'I1a' as sanity_query_name;
select distinct bs.account_record_id
from blocking_states bs
join analytics_account_transitions bos on bs.record_id = bos.blocking_state_record_id
where 1 = 1
and bs.is_active = 1
and (
     coalesce(bs.record_id, 'NULL') != coalesce(bos.blocking_state_record_id, 'NULL')
  or coalesce(bs.state, 'NULL') != coalesce(bos.state, 'NULL')
  /* TODO SubscriptionEvent is not an entity, we don't have that info yet
  or coalesce(bs.created_date, 'NULL') != coalesce(bos.created_date, 'NULL') */
  or (
    /* Tricky... Need to look at the account timezone */
        coalesce(date(bs.effective_date), 'NULL') != coalesce(date(bos.start_date), 'NULL')
    and coalesce(date(bs.effective_date), 'NULL') != coalesce(date_add(date(bos.start_date), INTERVAL 1 DAY), 'NULL')
    and coalesce(date(bs.effective_date), 'NULL') != coalesce(date_sub(date(bos.start_date), INTERVAL 1 DAY), 'NULL')
  )
  or coalesce(bs.account_record_id, 'NULL') != coalesce(bos.account_record_id, 'NULL')
  or coalesce(bs.tenant_record_id, 'NULL') != coalesce(bos.tenant_record_id, 'NULL')
)
;

select 'I1b' as sanity_query_name;
select distinct bos.account_record_id
from analytics_account_transitions bos
join blocking_states bs on bs.record_id = bos.blocking_state_record_id
where 1 = 1
and bs.is_active = 1
and (
     coalesce(bs.record_id, 'NULL') != coalesce(bos.blocking_state_record_id, 'NULL')
  or coalesce(bs.state, 'NULL') != coalesce(bos.state, 'NULL')
  /* TODO SubscriptionEvent is not an entity, we don't have that info yet
  or coalesce(bs.created_date, 'NULL') != coalesce(bos.created_date, 'NULL') */
  /* Tricky... Need to look at the account timezone */
  or (coalesce(date(bs.effective_date), 'NULL') != coalesce(date(bos.start_date), 'NULL')
  and coalesce(date(bs.effective_date), 'NULL') != coalesce(date_add(date(bos.start_date), INTERVAL 1 DAY), 'NULL')
  and coalesce(date(bs.effective_date), 'NULL') != coalesce(date_sub(date(bos.start_date), INTERVAL 1 DAY), 'NULL'))
  or coalesce(bs.account_record_id, 'NULL') != coalesce(bos.account_record_id, 'NULL')
  or coalesce(bs.tenant_record_id, 'NULL') != coalesce(bos.tenant_record_id, 'NULL')
)
;

select 'I2' as sanity_query_name;
select distinct b.account_record_id
from analytics_account_transitions b
left outer join accounts a on a.id = b.account_id
where 1 = 1
and (
     coalesce(a.record_id) != coalesce(b.account_record_id, '')
  or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
  or coalesce(a.name, '') != coalesce(b.account_name, '')
)
;

select 'I3' as sanity_query_name;
select distinct b.account_record_id
from analytics_account_transitions b
join audit_log al on b.blocking_state_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'BLOCKING_STATES'
where 1 = 1
and (
     coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
  or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
  or coalesce(b.created_by, '') != coalesce(al.created_by, '')
)
;


-- SUBSCRIPTION TRANSITIONS

select 'J1' as sanity_query_name;
select distinct bst.account_record_id
from analytics_subscription_transitions bst
left outer join subscription_events se on bst.subscription_event_record_id = se.record_id
/* TODO Look at entilement rows only, ignore in-memory events and blocking states */
where (coalesce(bst.prev_service, 'entitlement-service') = 'entitlement-service'
and coalesce(bst.next_service, 'entitlement-service') = 'entitlement-service'
and bst.subscription_event_record_id is not null)
/* Tricky... Need to look at the account timezone */
and ((coalesce(date(se.requested_date), '') != coalesce(date(bst.requested_timestamp), '')
and coalesce(date(se.requested_date), '') != coalesce(date_add(date(bst.requested_timestamp), INTERVAL 1 DAY), '')
and coalesce(date(se.requested_date), '') != coalesce(date_sub(date(bst.requested_timestamp), INTERVAL 1 DAY), ''))
or (coalesce(date(se.effective_date), '') != coalesce(date(bst.next_start_date), '')
and coalesce(date(se.effective_date), '') != coalesce(date_add(date(bst.next_start_date), INTERVAL 1 DAY), '')
and coalesce(date(se.effective_date), '') != coalesce(date_sub(date(bst.next_start_date), INTERVAL 1 DAY), ''))
or coalesce(se.subscription_id, '') != coalesce(bst.subscription_id, '')
or coalesce(se.phase_name, '') != coalesce(bst.next_slug, '')
/* See https://github.com/killbill/killbill/issues/65: subscription_events won't have the pricelist but the SubscriptionEvent object will at runtime */
or (se.price_list_name is not null and se.price_list_name != coalesce(bst.next_price_list, ''))
/* TODO SubscriptionEvent is not an entity, we don't have that info yet (we currently look at audit logs but this doesn't work for in-memory events)
or coalesce(se.created_date, '') != coalesce(bst.created_date, '') */
or coalesce(se.account_record_id, '') != coalesce(bst.account_record_id, '')
or coalesce(se.tenant_record_id, '') != coalesce(bst.tenant_record_id, ''))
;

select 'J2' as sanity_query_name;
select distinct b.account_record_id
from analytics_subscription_transitions b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

select 'J4' as sanity_query_name;
select distinct b.account_record_id
from analytics_subscription_transitions b
join audit_log al on b.subscription_event_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'SUBSCRIPTION_EVENTS'
/* TODO Look at entilement rows only, ignore in-memory events and blocking states */
where (coalesce(b.prev_service, 'entitlement-service') = 'entitlement-service'
and coalesce(b.next_service, 'entitlement-service') = 'entitlement-service'
and b.subscription_event_record_id is not null)
and (coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, ''))
;

select 'J5a' as sanity_query_name;
select distinct account_record_id
from analytics_subscription_transitions
where 1 = 1
and event like 'START%'
and prev_product_name is not null
;

select 'J5b' as sanity_query_name;
select distinct account_record_id
from analytics_subscription_transitions
where 1 = 1
and event like 'START%'
and next_product_name is null
;

select 'J6' as sanity_query_name;
select distinct account_record_id
from analytics_subscription_transitions
where 1 = 1
and event like 'STOP%'
and prev_product_name is null
;

select 'J7' as sanity_query_name;
select distinct account_record_id
from analytics_subscription_transitions
where 1 = 1
and event like 'ERROR%'
;

select 'J8' as sanity_query_name;
select distinct account_record_id
from analytics_subscription_transitions
where 1 = 1
and prev_service != next_service
;

select 'J9' as sanity_query_name;
select distinct account_record_id
from analytics_subscription_transitions
where 1 = 1
and coalesce(prev_product_category, next_product_category) != coalesce(next_product_category, prev_product_category)
;

select 'J10' as sanity_query_name;
select distinct account_record_id
from analytics_subscription_transitions ast
join (
  select
    subscription_id
  , event
  , count(*)
  from analytics_subscription_transitions
  where 1 = 1
  and (event like 'START_%' or event like 'STOP_%')
  group by 1, 2
  having(count(*)) > 1
) duplicates using(subscription_id)
;

select 'J11' as sanity_query_name;
select distinct account_record_id
from analytics_subscription_transitions ast
join (
  select
    account_record_id
  , event
  , next_service
  from analytics_subscription_transitions
  where 1 = 1
  and (
       (event like '%ENTITLEMENT%' and next_service = 'billing-service')
    or (event like '%BILLING%' and next_service = 'entitlement-service')
  )
) wrong_service using(account_record_id)
;

-- BUNDLE FIELDS
/* table not currently used */


-- BUNDLE TAGS
/* table not currently used */

select 'K1: Validate consistency of states between payments and payment_transactions' as sanity_query_name;
select * from (
select
     p.id
    ,case when first_failure_record_id is not null then
         case when first_failure_ptrx.transaction_status in ('UNKNOWN','PLUGIN_FAILURE') then
              case when first_failure_ptrx.transaction_type='AUTHORIZE' then 'AUTH_ERRORED'
                   when first_failure_ptrx.transaction_type='CAPTURE' then 'CAPTURE_ERRORED'
                   when first_failure_ptrx.transaction_type='CHARGEBACK' then'CHARGEBACK_ERRORED'
                   when first_failure_ptrx.transaction_type='CREDIT' then 'CREDIT_ERRORED'
                   when first_failure_ptrx.transaction_type='PURCHASE' then 'PURCHASE_ERRORED'
                   when first_failure_ptrx.transaction_type='REFUND' then 'REFUND_ERRORED'
                   when first_failure_ptrx.transaction_type='VOID' then 'VOID_ERRORED'
              end
              when first_failure_ptrx.transaction_status='PAYMENT_FAILURE' then
              case when first_failure_ptrx.transaction_type='AUTHORIZE' then 'AUTH_FAILED'
                   when first_failure_ptrx.transaction_type='CAPTURE' then 'CAPTURE_FAILED'
                   when first_failure_ptrx.transaction_type='CHARGEBACK' then'CHARGEBACK_FAILED'
                   when first_failure_ptrx.transaction_type='CREDIT' then 'CREDIT_FAILED'
                   when first_failure_ptrx.transaction_type='PURCHASE' then 'PURCHASE_FAILED'
                   when first_failure_ptrx.transaction_type='REFUND' then 'REFUND_FAILED'
                   when first_failure_ptrx.transaction_type='VOID' then 'VOID_FAILED'
              end
         end
         when first_failure_record_id is null and last_success_record_id is not null then
              case when last_success_ptrx.transaction_type='AUTHORIZE' then 'AUTH_SUCCESS'
                   when last_success_ptrx.transaction_type='CAPTURE' then 'CAPTURE_SUCCESS'
                   when last_success_ptrx.transaction_type='CHARGEBACK' then'CHARGEBACK_SUCCESS'
                   when last_success_ptrx.transaction_type='CREDIT' then 'CREDIT_SUCCESS'
                   when last_success_ptrx.transaction_type='PURCHASE' then 'PURCHASE_SUCCESS'
                   when last_success_ptrx.transaction_type='REFUND' then 'REFUND_SUCCESS'
                   when last_success_ptrx.transaction_type='VOID' then 'VOID_SUCCESS'
              end
         when first_failure_record_id is null and last_success_record_id is null and first_record_id is not null then
         case when first_ptrx.transaction_status in ('UNKNOWN','PLUGIN_FAILURE') then
              case when first_ptrx.transaction_type='AUTHORIZE' then 'AUTH_ERRORED'
                   when first_ptrx.transaction_type='CAPTURE' then 'CAPTURE_ERRORED'
                   when first_ptrx.transaction_type='CHARGEBACK' then'CHARGEBACK_ERRORED'
                   when first_ptrx.transaction_type='CREDIT' then 'CREDIT_ERRORED'
                   when first_ptrx.transaction_type='PURCHASE' then 'PURCHASE_ERRORED'
                   when first_ptrx.transaction_type='REFUND' then 'REFUND_ERRORED'
                   when first_ptrx.transaction_type='VOID' then 'VOID_ERRORED'
              end
              when first_ptrx.transaction_status='PAYMENT_FAILURE' then
              case when first_ptrx.transaction_type='AUTHORIZE' then 'AUTH_FAILED'
                   when first_ptrx.transaction_type='CAPTURE' then 'CAPTURE_FAILED'
                   when first_ptrx.transaction_type='CHARGEBACK' then'CHARGEBACK_FAILED'
                   when first_ptrx.transaction_type='CREDIT' then 'CREDIT_FAILED'
                   when first_ptrx.transaction_type='PURCHASE' then 'PURCHASE_FAILED'
                   when first_ptrx.transaction_type='REFUND' then 'REFUND_FAILED'
                   when first_ptrx.transaction_type='VOID' then 'VOID_FAILED'
              end
         end
     end as expected_value
     ,p.state_name
from (
    select
         first_ptrx.payment_id
        ,first_ptrx.record_id first_record_id
        ,latest_success.record_id as last_success_record_id
        ,min(pt.record_id) as first_failure_record_id
    from (
        select
            pt.payment_id
           ,min(pt.record_id) record_id
        from payment_transactions pt
        group by 1
        ) first_ptrx
        left outer join (
            select
                 pt.payment_id
                ,max(pt.record_id) record_id
            from payment_transactions pt
            where 1=1
                and pt.transaction_status = 'SUCCESS'
            group by 1
        ) latest_success on first_ptrx.payment_id=latest_success.payment_id
        left outer join payment_transactions pt on
            pt.payment_id=latest_success.payment_id
            and pt.record_id>latest_success.record_id
            and pt.transaction_status != 'SUCCESS'
    group by 1,2
    ) ptrx
    inner join payments p on
        ptrx.payment_id = p.id
    left outer join payment_transactions first_ptrx on
        ptrx.first_record_id=first_ptrx.record_id
    left outer join payment_transactions last_success_ptrx on
        ptrx.last_success_record_id=last_success_ptrx.record_id
    left outer join payment_transactions first_failure_ptrx on
        ptrx.first_failure_record_id=first_failure_ptrx.record_id
) t1
where 1=1
    and expected_value != state_name
;

select 'K2: payments.last_success_state_name and payment_transactions.transaction_state and payment_transactions.transaction_status' as sanity_query_name;
select
    *
from (
select
    pt.transaction_type
    ,pt.transaction_status
    ,case when pt.transaction_type='AUTHORIZE' then 'AUTH_SUCCESS'
          when pt.transaction_type='CAPTURE' then 'CAPTURE_SUCCESS'
          when pt.transaction_type='CHARGEBACK' then'CHARGEBACK_SUCCESS'
          when pt.transaction_type='CREDIT' then 'CREDIT_SUCCESS'
          when pt.transaction_type='PURCHASE' then 'PURCHASE_SUCCESS'
          when pt.transaction_type='REFUND' then 'REFUND_SUCCESS'
          when pt.transaction_type='VOID' then 'VOID_SUCCESS'
     end as expected_value
    ,p.last_success_state_name
    ,count(1)
from (
select
     pt.payment_id
    ,max(pt.record_id) latest_record_id
from payment_transactions pt
where 1=1
    and pt.transaction_status = 'SUCCESS'
group by 1
) latest_pt
    inner join payment_transactions pt on
        latest_pt.payment_id=pt.payment_id
        and latest_pt.latest_record_id=pt.record_id
    inner join payments p on
        latest_pt.payment_id=p.id
group by 1,2,3,4
) t1
where 1=1
    and expected_value != last_success_state_name
;

select 'K3: At least on payment trx for each payment' as sanity_query_name;
select
    *
from
    payments p
    left outer join payment_transactions pt on
        p.id=pt.payment_id
where 1=1
    and pt.payment_id is null
;

select 'L1: Validate consistency between base payments tables and anlytics_payment_* tables' as sanity_query_name;
select
   a.tenant_record_id
  ,'analytics_payment_auths' as table_name
  ,sum(case when b.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when b.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
             and p.id=b.payment_id and p.external_key=b.payment_external_key and p.record_id=b.payment_number and p.account_id=b.account_id
            then 1 else 0 end) matches
  ,count(1) total
from
  payments p
  inner join payment_transactions a on
    p.id=a.payment_id
  left outer join analytics_payment_auths b on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='AUTHORIZE'
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_captures' as table_name
  ,sum(case when b.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when b.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
             and p.id=b.payment_id and p.external_key=b.payment_external_key and p.record_id=b.payment_number and p.account_id=b.account_id
            then 1 else 0 end) matches
  ,count(1) total
from
  payments p
  inner join payment_transactions a on
    p.id=a.payment_id
  left outer join analytics_payment_captures b on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='CAPTURE'
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_credits' as table_name
  ,sum(case when b.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when b.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
             and p.id=b.payment_id and p.external_key=b.payment_external_key and p.record_id=b.payment_number and p.account_id=b.account_id
            then 1 else 0 end) matches
  ,count(1) total
from
  payments p
  inner join payment_transactions a on
    p.id=a.payment_id
  left outer join analytics_payment_credits b on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='CREDIT' -- ??
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_chargebacks' as table_name
  ,sum(case when b.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when b.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
             and p.id=b.payment_id and p.external_key=b.payment_external_key and p.record_id=b.payment_number and p.account_id=b.account_id
            then 1 else 0 end) matches
  ,count(1) total
from
  payments p
  inner join payment_transactions a on
    p.id=a.payment_id
  left outer join analytics_payment_chargebacks b on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='CHARGEBACK' -- ??
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_purchases' as table_name
  ,sum(case when b.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when b.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
             and p.id=b.payment_id and p.external_key=b.payment_external_key and p.record_id=b.payment_number and p.account_id=b.account_id
            then 1 else 0 end) matches
  ,count(1) total
from
  payments p
  inner join payment_transactions a on
    p.id=a.payment_id
  left outer join analytics_payment_purchases b on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='PURCHASE'
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_refunds' as table_name
  ,sum(case when b.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when b.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
             and p.id=b.payment_id and p.external_key=b.payment_external_key and p.record_id=b.payment_number and p.account_id=b.account_id
            then 1 else 0 end) matches
  ,count(1) total
from
  payments p
  inner join payment_transactions a on
    p.id=a.payment_id
  left outer join analytics_payment_refunds b on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='REFUND'
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_voids' as table_name
  ,sum(case when b.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when b.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
             and p.id=b.payment_id and p.external_key=b.payment_external_key and p.record_id=b.payment_number and p.account_id=b.account_id
            then 1 else 0 end) matches
  ,count(1) total
from
  payments p
  inner join payment_transactions a on
    p.id=a.payment_id
  left outer join analytics_payment_voids b on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='VOID'
group by 1,2
;

select 'L2: Validate consistency between base payment_transactions tables and anlytics_payment_* tables' as sanity_query_name;select '' as sanity_query_name;
select
   a.tenant_record_id
  ,'analytics_payment_auths' as table_name
  ,sum(case when a.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when a.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
            then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_auths b
  left outer join payment_transactions a on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='AUTHORIZE'
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_captures' as table_name
  ,sum(case when a.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when a.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
            then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_captures b
  left outer join payment_transactions a on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='CAPTURE'
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_credits' as table_name
  ,sum(case when a.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when a.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
            then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_credits b
  left outer join payment_transactions a on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='CREDIT' -- ??
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_chargebacks' as table_name
  ,sum(case when a.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when a.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
            then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_chargebacks b
    left outer join payment_transactions a on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='CHARGEBACK' -- ??
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_purchases' as table_name
  ,sum(case when a.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when a.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
            then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_purchases b
    left outer join payment_transactions a on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='PURCHASE'
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_refunds' as table_name
  ,sum(case when a.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when a.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
            then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_refunds b
    left outer join payment_transactions a on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='REFUND'
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_voids' as table_name
  ,sum(case when a.payment_id is null then 1 else 0 end) as row_missing
  ,sum(case when a.payment_id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_transaction_id and a.transaction_external_key=b.payment_transaction_external_key
             and a.transaction_status=b.payment_transaction_status -- and a.amount=b.amount and a.currency=b.currency and a.tenant_record_id=b.tenant_record_id
            then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_voids b
    left outer join payment_transactions a on
    a.id=b.payment_transaction_id
where 1=1
  and a.transaction_type='VOID'
group by 1,2
;

select 'L3: Validate consistency between anlytics_payment_* tables and base payments tables' as sanity_query_name;
select
   a.tenant_record_id
  ,'analytics_payment_auths' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_id and a.record_id=b.payment_number and a.external_key=b.payment_external_key then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_auths b
  left outer join payments a on
    a.id=b.payment_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_captures' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_id and a.record_id=b.payment_number and a.external_key=b.payment_external_key then 1 else 0 end) matches  ,count(1) total
from
  analytics_payment_captures b
  left outer join payments a on
    a.id=b.payment_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_credits' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_id and a.record_id=b.payment_number and a.external_key=b.payment_external_key then 1 else 0 end) matches  ,count(1) total
from
  analytics_payment_credits b
  left outer join payments a on
    a.id=b.payment_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_chargebacks' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_id and a.record_id=b.payment_number and a.external_key=b.payment_external_key then 1 else 0 end) matches  ,count(1) total
from
  analytics_payment_chargebacks b
  left outer join payments a on
    a.id=b.payment_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_purchases' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_id and a.record_id=b.payment_number and a.external_key=b.payment_external_key then 1 else 0 end) matches  ,count(1) total
from
  analytics_payment_purchases b
  left outer join payments a on
    a.id=b.payment_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_refunds' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_id and a.record_id=b.payment_number and a.external_key=b.payment_external_key then 1 else 0 end) matches  ,count(1) total
from
  analytics_payment_refunds b
  left outer join payments a on
    a.id=b.payment_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_voids' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.payment_id and a.record_id=b.payment_number and a.external_key=b.payment_external_key then 1 else 0 end) matches  ,count(1) total
from
  analytics_payment_voids b
  left outer join payments a on
    a.id=b.payment_id
group by 1,2
;

select 'L4: Validate consistency between anlytics_payment_* tables and base accounts tables' as sanity_query_name;
select
   a.tenant_record_id
  ,'analytics_payment_auths' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.account_id and a.record_id=b.account_record_id and a.external_key=b.account_external_key then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_auths b
  left outer join accounts a on
    a.id=b.account_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_captures' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.account_id and a.record_id=b.account_record_id and a.external_key=b.account_external_key then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_captures b
  left outer join accounts a on
    a.id=b.account_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_credits' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.account_id and a.record_id=b.account_record_id and a.external_key=b.account_external_key then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_credits b
  left outer join accounts a on
    a.id=b.account_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_chargebacks' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.account_id and a.record_id=b.account_record_id and a.external_key=b.account_external_key then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_chargebacks b
  left outer join accounts a on
    a.id=b.account_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_purchases' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.account_id and a.record_id=b.account_record_id and a.external_key=b.account_external_key then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_purchases b
  left outer join accounts a on
    a.id=b.account_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_refunds' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.account_id and a.record_id=b.account_record_id and a.external_key=b.account_external_key then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_refunds b
  left outer join accounts a on
    a.id=b.account_id
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_voids' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id=b.account_id and a.record_id=b.account_record_id and a.external_key=b.account_external_key then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_voids b
  left outer join accounts a on
    a.id=b.account_id
group by 1,2
;

select 'L5: Validate consistency between anlytics_payment_* tables and base invoice_payments tables' as sanity_query_name;
select
   a.tenant_record_id
  ,'analytics_payment_auths' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_payment_id and a.record_id = b.invoice_payment_record_id and a.type = b.invoice_payment_type then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_auths b
  left outer join invoice_payments a on
    a.id=b.invoice_payment_id
where 1=1
  and b.invoice_payment_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_captures' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_payment_id and a.record_id = b.invoice_payment_record_id and a.type = b.invoice_payment_type then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_captures b
  left outer join invoice_payments a on
    a.id=b.invoice_payment_id
where 1=1
  and b.invoice_payment_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_credits' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_payment_id and a.record_id = b.invoice_payment_record_id and a.type = b.invoice_payment_type then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_credits b
  left outer join invoice_payments a on
    a.id=b.invoice_payment_id
where 1=1
  and b.invoice_payment_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_chargebacks' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_payment_id and a.record_id = b.invoice_payment_record_id and a.type = b.invoice_payment_type then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_chargebacks b
  left outer join invoice_payments a on
    a.id=b.invoice_payment_id
where 1=1
  and b.invoice_payment_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_purchases' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_payment_id and a.record_id = b.invoice_payment_record_id and a.type = b.invoice_payment_type then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_purchases b
  left outer join invoice_payments a on
    a.id=b.invoice_payment_id
where 1=1
  and b.invoice_payment_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_refunds' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_payment_id and a.record_id = b.invoice_payment_record_id and a.type = b.invoice_payment_type then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_refunds b
  left outer join invoice_payments a on
    a.id=b.invoice_payment_id
where 1=1
  and b.invoice_payment_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_voids' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_payment_id and a.record_id = b.invoice_payment_record_id and a.type = b.invoice_payment_type then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_voids b
  left outer join invoice_payments a on
    a.id=b.invoice_payment_id
where 1=1
  and b.invoice_payment_id is not null
group by 1,2
;

select 'L6: Validate consistency between anlytics_payment_* tables and base invoices tables' as sanity_query_name;
select
   a.tenant_record_id
  ,'analytics_payment_auths' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_id and a.currency = b.invoice_currency and a.record_id = b.invoice_number and a.created_by = b.invoice_created_date
                 and a.invoice_date = b.invoice_date and a.target_date = b.invoice_target_date then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_auths b
  left outer join invoices a on
    a.id=b.invoice_id
where 1=1
  and b.invoice_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_captures' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_id and a.currency = b.invoice_currency and a.record_id = b.invoice_number and a.created_by = b.invoice_created_date
                 and a.invoice_date = b.invoice_date and a.target_date = b.invoice_target_date then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_captures b
  left outer join invoices a on
    a.id=b.invoice_id
where 1=1
  and b.invoice_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_credits' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_id and a.currency = b.invoice_currency and a.record_id = b.invoice_number and a.created_by = b.invoice_created_date
                 and a.invoice_date = b.invoice_date and a.target_date = b.invoice_target_date then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_credits b
  left outer join invoices a on
    a.id=b.invoice_id
where 1=1
  and b.invoice_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_chargebacks' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_id and a.currency = b.invoice_currency and a.record_id = b.invoice_number and a.created_by = b.invoice_created_date
                 and a.invoice_date = b.invoice_date and a.target_date = b.invoice_target_date then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_chargebacks b
  left outer join invoices a on
    a.id=b.invoice_id
where 1=1
  and b.invoice_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_purchases' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_id and a.currency = b.invoice_currency and a.record_id = b.invoice_number and a.created_by = b.invoice_created_date
                 and a.invoice_date = b.invoice_date and a.target_date = b.invoice_target_date then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_purchases b
  left outer join invoices a on
    a.id=b.invoice_id
where 1=1
  and b.invoice_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_refunds' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_id and a.currency = b.invoice_currency and a.record_id = b.invoice_number and a.created_by = b.invoice_created_date
                 and a.invoice_date = b.invoice_date and a.target_date = b.invoice_target_date then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_refunds b
  left outer join invoices a on
    a.id=b.invoice_id
where 1=1
  and b.invoice_id is not null
group by 1,2
UNION
select
   a.tenant_record_id
  ,'analytics_payment_voids' as table_name
  ,sum(case when a.id is null then 1 else 0 end) as row_missing
  ,sum(case when a.id is not null then 1 else 0 end) as row_exists
  ,sum(case when a.id = b.invoice_id and a.currency = b.invoice_currency and a.record_id = b.invoice_number and a.created_by = b.invoice_created_date
                 and a.invoice_date = b.invoice_date and a.target_date = b.invoice_target_date then 1 else 0 end) matches
  ,count(1) total
from
  analytics_payment_voids b
  left outer join invoices a on
    a.id=b.invoice_id
where 1=1
  and b.invoice_id is not null
group by 1,2
;

select 'L7: Validate no duplicate rows in analytics_payment_*' as sanity_query_name;
select
    table_name as "Table Name"
    ,repeat_count as "Repeated Row Count"
from (
select 'analytics_accounts' as table_name, ifnull(count(1),0) as repeat_count from (
    select account_record_id from analytics_accounts a group by 1 having count(1)>1 ) b
UNION
select 'analytics_bundles' as table_name, count(1) from (
    select bundle_record_id from analytics_bundles a group by 1 having count(1)>1 ) b
UNION
select 'analytics_invoice_adjustments' as table_name, ifnull(count(1),0) as repeat_count from (
    select invoice_item_record_id from analytics_invoice_adjustments a group by 1 having count(1)>1 ) b
UNION
select 'analytics_invoice_credits' as table_name, ifnull(count(1),0) as repeat_count from (
    select invoice_item_record_id from analytics_invoice_credits a group by 1 having count(1)>1 ) b
UNION
select 'analytics_invoice_item_adjustments' as table_name, ifnull(count(1),0) as repeat_count from (
    select invoice_item_record_id from analytics_invoice_item_adjustments a group by 1 having count(1)>1 ) b
UNION
select 'analytics_invoice_items' as table_name, ifnull(count(1),0) as repeat_count from (
    select invoice_item_record_id from analytics_invoice_items a group by 1 having count(1)>1 ) b
UNION
select 'analytics_invoices' as table_name, ifnull(count(1),0) as repeat_count from (
    select invoice_record_id from analytics_invoices a group by 1 having count(1)>1 ) b
UNION
select 'analytics_payment_auths' as table_name, ifnull(count(1),0) as repeat_count from (
    select payment_transaction_id from analytics_payment_auths a group by 1 having count(1)>1 ) b
UNION
select 'analytics_payment_captures' as table_name, count(1) from (
    select payment_transaction_id from analytics_payment_captures a group by 1 having count(1)>1 ) b
UNION
select 'analytics_payment_credits' as table_name, count(1) from (
    select payment_transaction_id from analytics_payment_credits a group by 1 having count(1)>1 ) b
UNION
select 'analytics_payment_chargebacks' as table_name, count(1) from (
    select payment_transaction_id from analytics_payment_chargebacks a group by 1 having count(1)>1 ) b
UNION
select 'analytics_payment_purchases' as table_name, count(1) from (
    select payment_transaction_id from analytics_payment_purchases a group by 1 having count(1)>1 ) b
UNION
select 'analytics_payment_refunds' as table_name, count(1) from (
    select payment_transaction_id from analytics_payment_refunds a group by 1 having count(1)>1 ) b
UNION
select 'analytics_payment_voids' as table_name, count(1) from (
    select payment_transaction_id from analytics_payment_voids a group by 1 having count(1)>1 ) b
) c
order by 2 desc, 1
;
