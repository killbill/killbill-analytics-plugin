select 'G6ai' as sanity_query_name;
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
;

select 'G6bi' as sanity_query_name;
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
;

select 'G7i' as sanity_query_name;
select *
from analytics_payments bip
left outer join _zuora_payment_methods ppm on bip.plugin_pm_id = ppm.z_pm_id
where (coalesce(ppm.z_pm_id, 'NULL') != coalesce(bip.plugin_pm_id, 'NULL')
or coalesce(ppm.z_default, 'NULL') != coalesce(bip.plugin_pm_is_default, 'NULL')) and ppm.z_pm_id is not null -- workaround until we get plugin name, query will miss missing rows
;