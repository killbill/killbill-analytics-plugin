create or replace view v_report_payments_by_provider_sub2 as
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_auths
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_captures
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_chargebacks
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_credits
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_purchases
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_refunds
union
select distinct plugin_name,ifnull(plugin_property_4,'unknown') as merchant_account,ifnull(plugin_property_5,'unknown') as payment_method,tenant_record_id from analytics_payment_voids
;
