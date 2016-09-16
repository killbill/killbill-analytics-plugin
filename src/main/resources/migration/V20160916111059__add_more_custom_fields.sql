alter table analytics_payment_fields rename to analytics_invoice_payment_fields;
create table analytics_payment_fields (
  record_id serial unique
, custom_field_record_id bigint /*! unsigned */ default null
, payment_id varchar(36) default null
, name varchar(64) default null
, value varchar(255) default null
, created_date datetime default null
, created_by varchar(50) default null
, created_reason_code varchar(255) default null
, created_comments varchar(255) default null
, account_id varchar(36) default null
, account_name varchar(100) default null
, account_external_key varchar(50) default null
, account_record_id bigint /*! unsigned */ default null
, tenant_record_id bigint /*! unsigned */ default null
, report_group varchar(50) not null
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create index analytics_payment_fields_account_id on analytics_payment_fields(account_id);
create index analytics_payment_fields_account_record_id on analytics_payment_fields(account_record_id);
create index analytics_payment_fields_tenant_account_record_id on analytics_payment_fields(tenant_record_id, account_record_id);
create table analytics_payment_method_fields (
  record_id serial unique
, custom_field_record_id bigint /*! unsigned */ default null
, payment_method_id varchar(36) default null
, name varchar(64) default null
, value varchar(255) default null
, created_date datetime default null
, created_by varchar(50) default null
, created_reason_code varchar(255) default null
, created_comments varchar(255) default null
, account_id varchar(36) default null
, account_name varchar(100) default null
, account_external_key varchar(50) default null
, account_record_id bigint /*! unsigned */ default null
, tenant_record_id bigint /*! unsigned */ default null
, report_group varchar(50) not null
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create index analytics_payment_method_fields_account_id on analytics_payment_method_fields(account_id);
create index analytics_payment_method_fields_account_record_id on analytics_payment_method_fields(account_record_id);
create index analytics_payment_method_fields_tenant_account_record_id on analytics_payment_method_fields(tenant_record_id, account_record_id);
create table analytics_transaction_fields (
  record_id serial unique
, custom_field_record_id bigint /*! unsigned */ default null
, transaction_id varchar(36) default null
, name varchar(64) default null
, value varchar(255) default null
, created_date datetime default null
, created_by varchar(50) default null
, created_reason_code varchar(255) default null
, created_comments varchar(255) default null
, account_id varchar(36) default null
, account_name varchar(100) default null
, account_external_key varchar(50) default null
, account_record_id bigint /*! unsigned */ default null
, tenant_record_id bigint /*! unsigned */ default null
, report_group varchar(50) not null
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create index analytics_transaction_fields_account_id on analytics_transaction_fields(account_id);
create index analytics_transaction_fields_account_record_id on analytics_transaction_fields(account_record_id);
create index analytics_transaction_fields_tenant_account_record_id on analytics_transaction_fields(tenant_record_id, account_record_id);
