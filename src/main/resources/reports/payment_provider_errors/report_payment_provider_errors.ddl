create table report_payment_provider_errors as select * from v_report_payment_provider_errors limit 0;

drop procedure if exists refresh_report_payment_provider_errors;
DELIMITER //
CREATE PROCEDURE refresh_report_payment_provider_errors()
BEGIN
truncate report_payment_provider_errors;
insert into report_payment_provider_errors select * from v_report_payment_provider_errors;
END;
//
DELIMITER ;
