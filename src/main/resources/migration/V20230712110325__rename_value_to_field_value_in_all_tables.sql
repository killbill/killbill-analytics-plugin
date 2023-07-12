ALTER TABLE analytics_account_fields CHANGE COLUMN "value" field_value varchar(255) default null;
ALTER TABLE analytics_bundle_fields CHANGE COLUMN "value" field_value varchar(255) default null;
ALTER TABLE analytics_invoice_fields CHANGE COLUMN "value" field_value varchar(255) default null;
ALTER TABLE analytics_invoice_payment_fields CHANGE COLUMN "value" field_value varchar(255) default null;
ALTER TABLE analytics_payment_fields CHANGE COLUMN "value" field_value varchar(255) default null;
ALTER TABLE analytics_payment_method_fields CHANGE COLUMN "value" field_value varchar(255) default null;
ALTER TABLE analytics_transaction_fields CHANGE COLUMN "value" field_value varchar(255) default null;