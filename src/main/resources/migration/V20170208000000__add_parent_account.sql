alter table analytics_accounts add parent_account_id varchar(36) default null after account_external_key;
alter table analytics_accounts add parent_account_name varchar(100) default null after parent_account_id;
alter table analytics_accounts add parent_account_external_key varchar(255) default null after parent_account_name;
