create table report_invoice_aging as select * from v_report_invoice_aging limit 0;

drop procedure if exists refresh_report_invoice_aging;
DELIMITER //
CREATE PROCEDURE refresh_report_invoice_aging()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_invoice_aging;
  insert into report_invoice_aging select * from v_report_invoice_aging;
COMMIT;

END;
//
DELIMITER ;
