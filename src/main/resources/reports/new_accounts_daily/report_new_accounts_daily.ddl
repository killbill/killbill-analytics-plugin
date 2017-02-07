create table report_new_accounts_daily as select * from v_report_new_accounts_daily limit 0;

drop procedure if exists refresh_report_new_accounts_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_new_accounts_daily()
BEGIN
insert into report_new_accounts_daily select * from v_report_new_accounts_daily;
END;
//
DELIMITER ;
