drop table if exists report_churn_total_usd_monthly;
create table report_churn_total_usd_monthly (tenant_record_id int(11), day date, term varchar(50), count int(8));
