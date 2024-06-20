create table report_invoices_daily as select * from v_report_invoices_daily limit 0;

drop procedure if exists refresh_report_invoices_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_invoices_daily()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_invoices_daily;
  insert into report_invoices_daily select * from v_report_invoices_daily;
COMMIT;

END;
//
DELIMITER ;
