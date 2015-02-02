drop table if exists report_subscriptions_total_dollar_monthly;
create table report_subscriptions_total_dollar_monthly (tenant_record_id int(11), day date, term varchar(50), count int(10));
