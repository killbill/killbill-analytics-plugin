create table report_payments_by_provider_history as select * from v_report_payments_by_provider limit 0;

drop procedure if exists refresh_report_payments_by_provider_history;
DELIMITER //
CREATE PROCEDURE refresh_report_payments_by_provider_history()
BEGIN
insert into report_payments_by_provider_history select * from v_report_payments_by_provider;
END;
//
DELIMITER ;
