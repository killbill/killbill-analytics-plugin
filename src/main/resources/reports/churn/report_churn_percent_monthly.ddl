drop table if exists report_churn_percent_monthly;
create table report_churn_percent_monthly (day date, term varchar(50), count decimal(5,4));
