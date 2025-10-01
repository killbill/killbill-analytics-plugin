create table report_invoice_credits_daily as select * from v_report_invoice_credits_daily limit 0;

drop procedure if exists refresh_report_invoice_credits_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_invoice_credits_daily()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_invoice_credits_daily;
  insert into report_invoice_credits_daily select * from v_report_invoice_credits_daily;
COMMIT;

END;
//
DELIMITER ;
