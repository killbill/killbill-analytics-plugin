create table report_invoice_credits_monthly as select * from v_report_invoice_credits_monthly limit 0;

drop procedure if exists refresh_report_invoice_credits_monthly;
DELIMITER //
CREATE PROCEDURE refresh_report_invoice_credits_monthly()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_invoice_credits_monthly;
  insert into report_invoice_credits_monthly select * from v_report_invoice_credits_monthly;
COMMIT;

END;
//
DELIMITER ;
