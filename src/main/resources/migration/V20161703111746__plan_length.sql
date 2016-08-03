alter table analytics_subscription_transitions modify prev_phase varchar(255) default null;
alter table analytics_subscription_transitions modify next_phase varchar(255) default null;
alter table analytics_bundles modify current_phase varchar(255) default null;
alter table analytics_invoice_adjustments modify phase varchar(255) default null;
alter table analytics_invoice_items modify phase varchar(255) default null;
alter table analytics_invoice_item_adjustments modify phase varchar(255) default null;
alter table analytics_invoice_credits modify phase varchar(255) default null;