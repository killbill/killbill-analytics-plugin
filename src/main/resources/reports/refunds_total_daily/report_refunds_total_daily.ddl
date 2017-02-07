create table report_refunds_total_daily as select * from v_report_refunds_total_daily limit 0;

drop procedure if exists refresh_report_refunds_total_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_refunds_total_daily()
BEGIN
insert into report_refunds_total_daily select * from v_report_refunds_total_daily;
END;
//
DELIMITER ;
