CREATE INDEX idx_ai_tenant_bundle ON analytics_invoice_items(tenant_record_id, bundle_id, invoice_original_amount_charged, invoice_balance);
CREATE INDEX idx_ast_bundle_tenant_event ON analytics_subscription_transitions(bundle_id, tenant_record_id, event, next_start_date, next_end_date, next_billing_period);
CREATE INDEX idx_subs_id_tenant ON subscriptions(id, tenant_record_id);
CREATE INDEX idx_se_sub_tenant_type ON subscription_events(subscription_id, tenant_record_id, user_type);