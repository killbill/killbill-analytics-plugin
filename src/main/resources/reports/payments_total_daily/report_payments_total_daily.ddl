create table report_payments_total_daily as select * from v_report_payments_total_daily limit 0;

drop procedure if exists refresh_report_payments_total_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_payments_total_daily()
BEGIN
truncate report_payments_total_daily;
insert into report_payments_total_daily select * from v_report_payments_total_daily;
END;
//
DELIMITER ;
