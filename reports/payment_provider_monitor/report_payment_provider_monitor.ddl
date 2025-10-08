create table report_payment_provider_monitor as select * from v_report_payment_provider_monitor limit 0;

drop procedure if exists refresh_report_payment_provider_monitor;
DELIMITER //
CREATE PROCEDURE refresh_report_payment_provider_monitor()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
insert into report_payment_provider_monitor select * from v_report_payment_provider_monitor;

END;
//
DELIMITER ;
