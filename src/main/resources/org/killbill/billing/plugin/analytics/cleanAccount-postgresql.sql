CREATE OR REPLACE PROCEDURE cleanAnalyticsAccount(p_account_id varchar(36)) LANGUAGE plpgsql
AS $$
DECLARE
    v_account_record_id bigint;
    v_tenant_record_id bigint;

BEGIN
    SELECT record_id, tenant_record_id FROM accounts WHERE id = p_account_id into v_account_record_id, v_tenant_record_id;

    DELETE FROM analytics_account_fields WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_account_tags WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_account_transitions WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_accounts WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_bundle_fields WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_bundle_tags WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_bundles WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_invoice_adjustments WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_invoice_credits WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_invoice_fields WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_invoice_item_adjustments WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_invoice_items WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_invoice_payment_fields WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_invoice_tags WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_invoices WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_notifications WHERE search_key1 = v_account_record_id and search_key2 = v_tenant_record_id;
    DELETE FROM analytics_notifications_history WHERE search_key1 = v_account_record_id and search_key2 = v_tenant_record_id;
    DELETE FROM analytics_payment_auths WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_payment_captures WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_payment_chargebacks WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_payment_credits WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_payment_fields WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_payment_method_fields WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_payment_purchases WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_payment_refunds WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_payment_tags WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_payment_voids WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_subscription_transitions WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
    DELETE FROM analytics_transaction_fields WHERE account_record_id = v_account_record_id and tenant_record_id = v_tenant_record_id;
END
$$;

