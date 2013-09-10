-- ACCOUNTS

-- A1a
select *
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

-- A1b
select *
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

-- A2
select *
from analytics_accounts b
join account_history ah on b.account_record_id = ah.target_record_id
join audit_log al on ah.record_id = al.target_record_id and al.change_type = 'INSERT' and al.table_name = 'ACCOUNT_HISTORY'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- ACCOUNT FIELDS

-- K1a
select *
from custom_fields cf
left outer join analytics_account_fields b on cf.record_id = b.custom_field_record_id
where (coalesce(b.name, 'NULL') != coalesce(cf.field_name, 'NULL')
or coalesce(b.value, 'NULL') != coalesce(cf.field_value, 'NULL')
or coalesce(b.created_date, 'NULL') != coalesce(cf.created_date, 'NULL')
or coalesce(b.account_record_id, 'NULL') != coalesce(cf.account_record_id, 'NULL')
or coalesce(b.tenant_record_id, 'NULL') != coalesce(cf.tenant_record_id, 'NULL') )
and cf.object_type = 'ACCOUNT'
;

-- K1b
select *
from analytics_account_fields b
left outer join custom_fields cf on cf.record_id = b.custom_field_record_id
where coalesce(b.name, 'NULL') != coalesce(cf.field_name, 'NULL')
or coalesce(b.value, 'NULL') != coalesce(cf.field_value, 'NULL')
or coalesce(b.created_date, 'NULL') != coalesce(cf.created_date, 'NULL')
or coalesce(b.account_record_id, 'NULL') != coalesce(cf.account_record_id, 'NULL')
or coalesce(b.tenant_record_id, 'NULL') != coalesce(cf.tenant_record_id, 'NULL')
or cf.object_type != 'ACCOUNT'
;

-- K2
select *
from analytics_account_fields b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- K3
select *
from analytics_account_fields b
join custom_field_history cfh on b.custom_field_record_id = cfh.target_record_id
join audit_log al on cfh.record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'CUSTOM_FIELD_HISTORY'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- ACCOUNT TAGS

-- L1a
select *
from tags t
join tag_definitions td on t.tag_definition_id = td.id
left outer join analytics_account_tags b on t.record_id = b.tag_record_id
where (coalesce(b.tag_record_id, 'NULL') != coalesce(t.record_id, 'NULL')
or coalesce(b.name, 'NULL') != coalesce(td.name, 'NULL')
or coalesce(b.created_date, 'NULL') != coalesce(t.created_date, 'NULL')
or coalesce(b.account_record_id, 'NULL') != coalesce(t.account_record_id, 'NULL')
or coalesce(b.tenant_record_id, 'NULL') != coalesce(t.tenant_record_id, 'NULL'))
and t.object_type = 'ACCOUNT'
;

-- L1b
select *
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

-- L2
select *
from analytics_account_tags b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- L3
select *
from analytics_account_tags b
join tag_history th on b.tag_record_id = th.target_record_id
join audit_log al on th.record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'TAG_HISTORY'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- INVOICE ADJUSTMENTS

-- B1a
-- this will find things it thinks should be in bia but it's correct that they're not there
select *
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

-- B1b
select *
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

-- B2
select *
from analytics_invoice_adjustments b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- B3
select *
from analytics_invoice_adjustments b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

-- B4
select *
from analytics_invoice_adjustments b
left outer join bundles bndl on b.bundle_id = bndl.id
where coalesce(bndl.external_key, 'NULL') != coalesce(b.bundle_external_key, 'NULL')
;

-- B5
select *
from analytics_invoice_adjustments b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

-- B6
select *
from analytics_invoice_adjustments b
join audit_log al on b.invoice_item_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_ITEMS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- INVOICE ITEMS

-- C1a
select *
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

-- C1b
select *
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

-- C2
select *
from analytics_invoice_items b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- C3
select *
from analytics_invoice_items b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

-- C4
select *
from analytics_invoice_items b
left outer join bundles bndl on b.bundle_id = bndl.id
where coalesce(bndl.external_key, 'NULL') != coalesce(b.bundle_external_key, 'NULL')
;

-- C5
select *
from analytics_invoice_items b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

-- C6
select *
from analytics_invoice_items b
join audit_log al on b.invoice_item_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_ITEMS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- INVOICE ITEM ADJUSTMENTS

-- D1a
select *
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

-- D1b
select *
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

-- D2
select *
from analytics_invoice_item_adjustments b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- D3
select *
from analytics_invoice_item_adjustments b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

-- D4
select *
from analytics_invoice_item_adjustments b
left outer join bundles bndl on b.bundle_id = bndl.id
where coalesce(bndl.external_key, 'NULL') != coalesce(b.bundle_external_key, 'NULL')
and b.bundle_id is not null
;

-- D5
select *
from analytics_invoice_item_adjustments b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

-- D6
select *
from analytics_invoice_item_adjustments b
join audit_log al on b.invoice_item_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_ITEMS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- INVOICE CREDITS

-- E1a
select *
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

-- E1b
select *
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

-- E2
select *
from analytics_invoice_credits b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- E3
select *
from analytics_invoice_credits b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

-- E4
select *
from analytics_invoice_credits b
left outer join bundles bndl on b.bundle_id = bndl.id
where coalesce(bndl.external_key, 'NULL') != coalesce(b.bundle_external_key, 'NULL')
;

-- E5
select *
from analytics_invoice_credits b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

-- E6
select *
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

-- F1a
select *
from invoices i
left outer join analytics_invoices on i.id = bin.invoice_id
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

-- F1b
select *
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

-- F2
select *
from analytics_invoices b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- F3a
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

-- F3b
select
  bin.invoice_id
, bin.original_amount_charged
, sum(bii.amount)
from analytics_invoice_items bii
join analytics_invoices bin on bii.invoice_id = bin.invoice_id and bii.created_date = bin.created_date
group by bin.invoice_id, bin.original_amount_charged
having sum(bii.amount) != bin.original_amount_charged
;

-- F3c
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

-- F3d
select
  *
, bip_sum invoice_amount_paid
from (
  select
    invoice_id
  , invoice_amount_paid
  , sum(coalesce(amount,0)) bip_sum
  from analytics_payments bip
  group by invoice_id, invoice_amount_paid
) bip_sum
where bip_sum != bip_sum.invoice_amount_paid
;

-- F3e
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
  from bin
) bin
left outer join (
  select
    invoice_id
  , sum(coalesce(amount,0)) bipc_sum
  from analytics_payments bipc
  group by invoice_id
) bipc_sum using (invoice_id)
left outer join (
  select
    invoice_id
  , sum(coalesce(amount,0)) bipr_sum
  from analytics_refunds bipr
  group by invoice_id
) bipr_sum using (invoice_id)
where bipc_sum + bipr_sum != bin.amount_refunded
;

-- F3f
select *
from analytics_invoices bin
where bin.amount_charged + bin.amount_credited - bin.amount_paid - bin.amount_refunded != bin.balance
and (bin.amount_charged != 0 and bin.amount_paid != 0 and bin.amount_refunded != 0 and bin.balance != 0) -- deal w / acct credit
;


-- F6
select *
from analytics_invoices b
join audit_log al on b.invoice_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICES'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- PAYMENTS

-- G1a
select *
from invoice_payments ip
left outer join analytics_payments bip on ip.id = bip.invoice_payment_id
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

-- G1b
select *
from analytics_payments bip
left outer join invoice_payments ip on ip.id = bip.invoice_payment_id
where coalesce(ip.record_id, 'NULL') != coalesce(bip.invoice_payment_record_id, 'NULL')
or coalesce(ip.ID, 'NULL') != coalesce(bip.invoice_payment_id, 'NULL')
or coalesce(ip.invoice_id, 'NULL') != coalesce(bip.invoice_id, 'NULL')
or coalesce(ip.type, 'NULL') != coalesce(bip.invoice_payment_type, 'NULL')
or coalesce(ip.linked_invoice_payment_id, 'NULL') != coalesce(bip.linked_invoice_payment_id, 'NULL')
or coalesce(ip.amount, 'NULL') != coalesce(bip.amount, 'NULL')
or coalesce(ip.currency, 'NULL') != coalesce(bip.currency, 'NULL')
or coalesce(ip.created_date, 'NULL') != coalesce(bip.created_date, 'NULL')
or coalesce(ip.account_record_id, 'NULL') != coalesce(bip.account_record_id, 'NULL')
or coalesce(ip.tenant_record_id, 'NULL') != coalesce(bip.tenant_record_id, 'NULL')
or bip.invoice_payment_type != 'ATTEMPT'
;

-- G2
select *
from analytics_payments b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- G3
select *
from analytics_payments b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

-- G4
select *
from analytics_payments b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

-- G5
select *
from analytics_payments bip
left outer join invoice_payments ip on bip.invoice_payment_id = ip.id
left outer join payments p on ip.payment_id = p.id
where coalesce(p.record_id, 'NULL') != coalesce(bip.payment_number, 'NULL')
;

-- G6ai
-- Zuora
/*
select *
from bip
left outer join invoice_payments ip on bip.invoice_payment_id = ip.id
left outer join _zuora_payments pp on ip.payment_id = pp.kb_p_id
where (coalesce(pp.z_created_date, 'NULL') != coalesce(bip.plugin_created_date, 'NULL')
or coalesce(pp.z_effective_date, 'NULL') != coalesce(bip.plugin_effective_date, 'NULL')
or coalesce(pp.z_status, 'NULL') != coalesce(bip.plugin_status, 'NULL')
or coalesce(pp.z_gateway_error, 'NULL') != coalesce(bip.plugin_gateway_error, 'NULL')
or coalesce(pp.z_gateway_error_code, 'NULL') != coalesce(bip.plugin_gateway_error_code, 'NULL')
or coalesce(pp.z_reference_id, 'NULL') != coalesce(bip.plugin_first_reference_id, 'NULL')
or coalesce(pp.z_snd_reference_id, 'NULL') != coalesce(bip.plugin_second_reference_id, 'NULL') ) and pp.kb_p_id is not null -- workaround until we get plugin name, query will miss missing rows
*/

-- Litle

-- PayPal

-- G6bi

-- Zuora
/*
select *
from _zuora_payments pp
left outer join invoice_payments ip on ip.payment_id = pp.kb_p_id
left outer join analytics_payments bip on bip.invoice_payment_id = ip.id
where (coalesce(pp.z_created_date, 'NULL') != coalesce(bip.plugin_created_date, 'NULL')
or coalesce(pp.z_effective_date, 'NULL') != coalesce(bip.plugin_effective_date, 'NULL')
or coalesce(pp.z_status, 'NULL') != coalesce(bip.plugin_status, 'NULL')
or coalesce(pp.z_gateway_error, 'NULL') != coalesce(bip.plugin_gateway_error, 'NULL')
or coalesce(pp.z_gateway_error_code, 'NULL') != coalesce(bip.plugin_gateway_error_code, 'NULL')
or coalesce(pp.z_reference_id, 'NULL') != coalesce(bip.plugin_first_reference_id, 'NULL')
or coalesce(pp.z_snd_reference_id, 'NULL') != coalesce(bip.plugin_second_reference_id, 'NULL')) and z_status != 'Error'
*/

-- Litle

-- PayPal

-- G7i
--Zuora
/*
select *
from analytics_payments bip
left outer join _zuora_payment_methods ppm on bip.plugin_pm_id = ppm.z_pm_id		
where (coalesce(ppm.z_pm_id, 'NULL') != coalesce(bip.plugin_pm_id, 'NULL')
or coalesce(ppm.z_default, 'NULL') != coalesce(bip.plugin_pm_is_default, 'NULL')) and ppm.z_pm_id is not null -- workaround until we get plugin name, query will miss missing rows
*/

-- Litle

-- PayPal

-- G8
select *
from analytics_payments b
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

-- H1a
select *
from invoice_payments ip
left outer join analytics_chargebacks on ip.id = bipc.invoice_payment_id
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

-- H1b
select *
from analytics_chargebacks bipc
left outer join invoice_payments ip on ip.id = bipc.invoice_payment_id
where coalesce(ip.record_id, 'NULL') != coalesce(bipc.invoice_payment_record_id, 'NULL')
or coalesce(ip.ID, 'NULL') != coalesce(bipc.invoice_payment_id, 'NULL')
or coalesce(ip.invoice_id, 'NULL') != coalesce(bipc.invoice_id, 'NULL')
or coalesce(ip.type, 'NULL') != coalesce(bipc.invoice_payment_type, 'NULL')
or coalesce(ip.linked_invoice_payment_id, 'NULL') != coalesce(bipc.linked_invoice_payment_id, 'NULL')
or coalesce(ip.amount, 'NULL') != coalesce(bipc.amount, 'NULL')
or coalesce(ip.currency, 'NULL') != coalesce(bipc.currency, 'NULL')
or coalesce(ip.created_date, 'NULL') != coalesce(bipc.created_date, 'NULL')
or coalesce(ip.account_record_id, 'NULL') != coalesce(bipc.account_record_id, 'NULL')
or coalesce(ip.tenant_record_id, 'NULL') != coalesce(bipc.tenant_record_id, 'NULL')
or bipc.invoice_payment_type != 'CHARGED_BACK'
;

-- H2
select *
from analytics_chargebacks b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- H3
select *
from analytics_chargebacks b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

-- H4
select *
from analytics_chargebacks b
left outer join bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

-- H5
select *
from analytics_chargebacks bipc
left outer join invoice_payments ip on bipc.invoice_payment_id = ip.id
left outer join payments p on ip.payment_id = p.id
where coalesce(p.record_id, 'NULL') != coalesce(bipc.payment_number, 'NULL')
;

-- H8
select *
from analytics_chargebacks b
join audit_log al on b.invoice_payment_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_PAYMENTS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- REFUNDS

-- H1a
select *
from invoice_payments ip
left outer join analytics_refunds bipr on ip.id = bipr.invoice_payment_id
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

-- H1b
select *
from analytics_refunds bipr
left outer join invoice_payments ip on ip.id = bipr.invoice_payment_id
where coalesce(ip.record_id, 'NULL') != coalesce(bipr.invoice_payment_record_id, 'NULL')
or coalesce(ip.id, 'NULL') != coalesce(bipr.invoice_payment_id, 'NULL')
or coalesce(ip.invoice_id, 'NULL') != coalesce(bipr.invoice_id, 'NULL')
or coalesce(ip.type, 'NULL') != coalesce(bipr.invoice_payment_type, 'NULL')
or coalesce(ip.linked_invoice_payment_id, 'NULL') != coalesce(bipr.linked_invoice_payment_id, 'NULL')
or coalesce(ip.amount, 'NULL') != coalesce(bipr.amount, 'NULL')
or coalesce(ip.currency, 'NULL') != coalesce(bipr.currency, 'NULL')
or coalesce(ip.created_date, 'NULL') != coalesce(bipr.created_date, 'NULL')
or coalesce(ip.account_record_id, 'NULL') != coalesce(bipr.account_record_id, 'NULL')
or coalesce(ip.tenant_record_id, 'NULL') != coalesce(bipr.tenant_record_id, 'NULL')
or bipr.invoice_payment_type != 'REFUND'
;

-- H2
select *
from analytics_refunds b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- H3
select *
from analytics_refunds b
left outer join invoices i on i.id = b.invoice_id
where coalesce(i.record_id, 'NULL') != coalesce(b.invoice_number, 'NULL')
or coalesce(i.created_date, 'NULL') != coalesce(b.invoice_created_date, 'NULL')
or coalesce(i.invoice_date, 'NULL') != coalesce(b.invoice_date, 'NULL')
or coalesce(i.target_date, 'NULL') != coalesce(b.invoice_target_date, 'NULL')
or coalesce(i.currency, 'NULL') != coalesce(b.invoice_currency, 'NULL')
;

-- H4
select *
from analytics_refunds b
left outer join analytics_invoices bin on b.invoice_id = bin.invoice_id
where b.invoice_balance != bin.balance
or b.invoice_amount_paid != bin.amount_paid
or b.invoice_amount_charged != bin.amount_charged
or b.invoice_original_amount_charged != bin.original_amount_charged
or b.invoice_amount_credited != bin.amount_credited
;

-- H5
select *
from analytics_refunds bipr
left outer join invoice_payments ip on bipr.invoice_payment_id = ip.id
left outer join payments p on ip.payment_id = p.id
where coalesce(p.record_id, 'NULL') != coalesce(bipr.payment_number, 'NULL')
;

-- H8
select *
from analytics_refunds b
join audit_log al on b.invoice_payment_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'INVOICE_PAYMENTS'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- ACCOUNT TRANSITIONS

-- I1a TODO
select *
from blocking_states bs
left outer join analytics_account_transitions bos on bs.record_id = bos.blocking_state_record_id
where (coalesce(bs.record_id, 'NULL') != coalesce(bos.blocking_state_record_id, 'NULL')
or coalesce(bs.state, 'NULL') != coalesce(bos.state, 'NULL')
/* TODO SubscriptionEvent is not an entity, we don't have that info yet
or coalesce(bs.created_date, 'NULL') != coalesce(bos.created_date, 'NULL') */
/* Tricky... Need to look at the account timezone */
or (coalesce(date(bs.effective_date), 'NULL') != coalesce(date(bos.start_date), 'NULL')
and coalesce(date(bs.effective_date), 'NULL') != coalesce(date_add(date(bos.start_date), INTERVAL 1 DAY), 'NULL')
and coalesce(date(bs.effective_date), 'NULL') != coalesce(date_sub(date(bos.start_date), INTERVAL 1 DAY), 'NULL'))
or coalesce(bs.account_record_id, 'NULL') != coalesce(bos.account_record_id, 'NULL')
or coalesce(bs.tenant_record_id, 'NULL') != coalesce(bos.tenant_record_id, 'NULL'))
and coalesce(bs.type, 'NULL') = 'ACCOUNT'
;

-- I1b
select *
from analytics_account_transitions bos
left outer join blocking_states bs on bs.record_id = bos.blocking_state_record_id
where coalesce(bs.record_id, 'NULL') != coalesce(bos.blocking_state_record_id, 'NULL')
or coalesce(bs.type, 'NULL') != 'ACCOUNT'
or coalesce(bs.state, 'NULL') != coalesce(bos.state, 'NULL')
/* TODO SubscriptionEvent is not an entity, we don't have that info yet
or coalesce(bs.created_date, 'NULL') != coalesce(bos.created_date, 'NULL') */
/* Tricky... Need to look at the account timezone */
or (coalesce(date(bs.effective_date), 'NULL') != coalesce(date(bos.start_date), 'NULL')
and coalesce(date(bs.effective_date), 'NULL') != coalesce(date_add(date(bos.start_date), INTERVAL 1 DAY), 'NULL')
and coalesce(date(bs.effective_date), 'NULL') != coalesce(date_sub(date(bos.start_date), INTERVAL 1 DAY), 'NULL'))
or coalesce(bs.account_record_id, 'NULL') != coalesce(bos.account_record_id, 'NULL')
or coalesce(bs.tenant_record_id, 'NULL') != coalesce(bos.tenant_record_id, 'NULL')
;

-- I2
select *
from analytics_account_transitions b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- I3
select *
from analytics_account_transitions b
join audit_log al on b.blocking_state_record_id = al.target_record_id and al.change_type = 'INSERT' and table_name = 'BLOCKING_STATES'
where coalesce(b.created_reason_code, 'NULL') != coalesce(al.reason_code, 'NULL')
or coalesce(b.created_comments, 'NULL') != coalesce(al.comments, 'NULL')
or coalesce(b.created_by, '') != coalesce(al.created_by, '')
;


-- SUBSCRIPTION TRANSITIONS

-- J1
select *
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

-- J2
select *
from analytics_subscription_transitions b
left outer join accounts a on a.id = b.account_id
where coalesce(a.record_id) != coalesce(b.account_record_id, '')
or coalesce(a.id, '') != coalesce(b.account_id, '')
or coalesce(a.external_key, '') != coalesce(b.account_external_key, '')
or coalesce(a.name, '') != coalesce(b.account_name, '')
;

-- J4
select *
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


-- BUNDLE FIELDS
/* table not currently used */


-- BUNDLE TAGS
/* table not currently used */
