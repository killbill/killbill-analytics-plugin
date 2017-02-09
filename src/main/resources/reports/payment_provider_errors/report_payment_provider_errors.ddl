create table report_payment_provider_errors as select * from v_report_payment_provider_errors limit 0;

drop procedure if exists refresh_report_payment_provider_errors;
DELIMITER //
CREATE PROCEDURE refresh_report_payment_provider_errors()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_payment_provider_errors;
  insert into report_payment_provider_errors select * from v_report_payment_provider_errors;
COMMIT;

END;
//
DELIMITER ;
