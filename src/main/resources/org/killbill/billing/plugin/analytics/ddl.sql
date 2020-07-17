/*! SET default_storage_engine=INNODB */;

drop table if exists analytics_accounts_snapshot;
create table analytics_accounts_snapshot (
  account_id varchar(36) unique not null
, balance numeric(10, 4) not null default 0
, cba numeric(10, 4) not null default 0
, currency varchar(3) default null
, manual_pay bool not null default false
, auto_pay_off bool not null default false
, auto_invoicing_off bool not null default false
, auto_invoicing_draft bool not null default false
, auto_invoicing_reuse_draft bool not null default false
, overdue_enforcement_off bool not null default false
, test bool not null default false
, partner bool not null default false
, closed bool not null default false
, cf1_value varchar(255) default null
, cf2_value varchar(255) default null
, cf3_value varchar(255) default null
, primary key(account_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;

drop table if exists analytics_subscriptions_snapshot;
create table analytics_subscriptions_snapshot (
  subscription_id varchar(36) unique not null
, bcd int default null
, product_name varchar(255) not null
, product_category varchar(255) not null
, product_phase_name varchar(255) not null
, product_phase_type varchar(255) not null
, plan_name varchar(255) not null
, pricelist_name varchar(255) not null
, catalog_name varchar(255) not null
, catalog_version datetime not null
, entitlement_state varchar(255) not null
, entitlement_start_date datetime not null
, entitlement_end_date datetime default null
, billing_start_date datetime not null
, billing_end_date datetime default null
, cf1_value varchar(255) default null
, cf2_value varchar(255) default null
, cf3_value varchar(255) default null
, created_date datetime not null
, created_by varchar(50) not null
, created_reason_code varchar(255) not null
, created_comments varchar(255) not null
, primary key(subscription_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;

drop table if exists analytics_subscriptions_transitions_snapshot;
create table analytics_subscriptions_transitions_snapshot (
  subscription_event_record_id bigint /*! unsigned */ unique not null
, subscription_id varchar(36) not null
, requested_timestamp date not null
, event varchar(50) not null
, service varchar(50) not null
, prev_bcd int default null
, prev_product_name varchar(255) default null
, prev_product_category varchar(255) default null
, prev_product_phase_name varchar(255) default null
, prev_product_phase_type varchar(255) default null
, prev_plan_name varchar(255) default null
, prev_pricelist_name varchar(255) default null
, prev_catalog_name varchar(255) default null
, prev_catalog_version datetime default null
, prev_entitlement_state varchar(50) default null
, prev_entitlement_start_date datetime default null
, next_bcd int default null
, next_product_name varchar(255) default null
, next_product_category varchar(255) default null
, next_product_phase_name varchar(255) default null
, next_product_phase_type varchar(255) default null
, next_plan_name varchar(255) default null
, next_pricelist_name varchar(255) default null
, next_catalog_name varchar(255) default null
, next_catalog_version datetime default null
, next_entitlement_state varchar(50) default null
, next_entitlement_end_date datetime default null
, created_date datetime not null
, created_by varchar(50) not null
, created_reason_code varchar(255) not null
, created_comments varchar(255) not null
, primary key(subscription_event_record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create index ast_snapshot_subscription_id on analytics_subscriptions_transitions_snapshot(subscription_id);
create index ast_snapshot_service on analytics_subscriptions_transitions_snapshot(service);

drop table if exists analytics_invoices_snapshot;
create table analytics_invoices_snapshot (
  invoice_id varchar(36) unique not null
, raw_balance numeric(10, 4) not null default 0
, balance numeric(10, 4) not null default 0
, amount_paid numeric(10, 4) not null default 0
, original_amount_charged numeric(10, 4) not null default 0
, amount_charged numeric(10, 4) not null default 0
, amount_credit numeric(10, 4) not null default 0
, amount_refunded numeric(10, 4) not null default 0
, currency varchar(3) default null
, written_off bool not null default false
, cf1_value varchar(255) default null
, cf2_value varchar(255) default null
, cf3_value varchar(255) default null
, created_date datetime not null
, created_by varchar(50) not null
, created_reason_code varchar(255) not null
, created_comments varchar(255) not null
, primary key(invoice_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;

drop table if exists analytics_notifications;
create table analytics_notifications (
  record_id serial unique
, class_name varchar(256) not null
, event_json varchar(2048) not null
, user_token varchar(36)
, created_date datetime not null
, creating_owner varchar(50) not null
, processing_owner varchar(50) default null
, processing_available_date datetime default null
, processing_state varchar(14) default 'AVAILABLE'
, error_count int /*! unsigned */ DEFAULT 0
, search_key1 int /*! unsigned */ default null
, search_key2 int /*! unsigned */ default null
, queue_name varchar(64) not null
, effective_date datetime not null
, future_user_token varchar(36)
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create index analytics_notifications_comp_where on analytics_notifications(effective_date, processing_state, processing_owner, processing_available_date);
create index analytics_notifications_update on analytics_notifications(processing_state,processing_owner,processing_available_date);
create index analytics_notifications_get_ready on analytics_notifications(effective_date,created_date);
create index analytics_notifications_search_keys on analytics_notifications(search_key2, search_key1);

drop table if exists analytics_notifications_history;
create table analytics_notifications_history (
  record_id serial unique
, class_name varchar(256) not null
, event_json varchar(2048) not null
, user_token varchar(36)
, created_date datetime not null
, creating_owner varchar(50) not null
, processing_owner varchar(50) default null
, processing_available_date datetime default null
, processing_state varchar(14) default 'AVAILABLE'
, error_count int /*! unsigned */ DEFAULT 0
, search_key1 int /*! unsigned */ default null
, search_key2 int /*! unsigned */ default null
, queue_name varchar(64) not null
, effective_date datetime not null
, future_user_token varchar(36)
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;

drop table if exists analytics_reports;
create table analytics_reports (
  record_id serial unique
, report_name varchar(100) not null
, report_pretty_name varchar(256) default null
, report_type varchar(24) not null default 'TIMELINE'
, source_table_name varchar(256) not null
, refresh_procedure_name varchar(256) default null
, refresh_frequency varchar(50) default null
, refresh_hour_of_day_gmt smallint default null
, primary key(record_id)
) /*! CHARACTER SET utf8 COLLATE utf8_bin */;
create unique index analytics_reports_report_name on analytics_reports(report_name);
