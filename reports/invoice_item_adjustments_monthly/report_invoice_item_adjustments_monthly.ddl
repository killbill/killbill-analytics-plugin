create table report_invoice_item_adjustments_monthly as select * from v_report_invoice_item_adjustments_monthly limit 0;

drop procedure if exists refresh_report_invoice_item_adjustments_monthly;
DELIMITER //
CREATE PROCEDURE refresh_report_invoice_item_adjustments_monthly()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_invoice_item_adjustments_monthly;
  insert into report_invoice_item_adjustments_monthly select * from v_report_invoice_item_adjustments_monthly;
COMMIT;

END;
//
DELIMITER ;
