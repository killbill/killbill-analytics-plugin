create or replace view v_report_payments_by_provider_sub2 as
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_auths force index(analytics_payment_auths_date_trid_plugin_name) where created_date > now() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_captures force index(analytics_payment_captures_date_trid_plugin_name) where created_date > now() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_chargebacks force index(analytics_payment_chargebacks_date_trid_plugin_name) where created_date > now() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_credits force index(analytics_payment_credits_date_trid_plugin_name) where created_date > now() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_purchases force index(analytics_payment_purchases_date_trid_plugin_name) where created_date > now() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_refunds force index(analytics_payment_refunds_date_trid_plugin_name) where created_date > now() - interval '7' day
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_voids force index(analytics_payment_voids_date_trid_plugin_name) where created_date > now() - interval '7' day
;
