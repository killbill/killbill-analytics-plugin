alter table analytics_payment_auths modify plugin_gateway_error text DEFAULT NULL;
alter table analytics_payment_captures modify plugin_gateway_error text DEFAULT NULL;
alter table analytics_payment_purchases modify plugin_gateway_error text DEFAULT NULL;
alter table analytics_payment_refunds modify plugin_gateway_error text DEFAULT NULL;
alter table analytics_payment_credits modify plugin_gateway_error text DEFAULT NULL;
alter table analytics_payment_chargebacks modify plugin_gateway_error text DEFAULT NULL;
alter table analytics_payment_voids modify plugin_gateway_error text DEFAULT NULL;
