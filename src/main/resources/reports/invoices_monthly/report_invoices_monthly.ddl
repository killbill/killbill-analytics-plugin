create table report_invoices_monthly as select * from v_report_invoices_monthly limit 0;

drop procedure if exists refresh_report_invoices_monthly;
DELIMITER //
CREATE PROCEDURE refresh_report_invoices_monthly()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_invoices_monthly;
  insert into report_invoices_monthly select * from v_report_invoices_monthly;
COMMIT;

END;
//
DELIMITER ;
