create table report_invoice_aging_no_payment as select * from v_report_invoice_aging_no_payment limit 0;

drop procedure if exists refresh_report_invoice_aging_no_payment;
DELIMITER //
CREATE PROCEDURE refresh_report_invoice_aging_no_payment()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_invoice_aging_no_payment;
  insert into report_invoice_aging_no_payment select * from v_report_invoice_aging_no_payment;
COMMIT;

END;
//
DELIMITER ;
