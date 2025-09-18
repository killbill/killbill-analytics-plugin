create table report_active_by_product_term_monthly as select * from v_report_active_by_product_term_monthly limit 0;

drop procedure if exists refresh_report_active_by_product_term_monthly;
DELIMITER //
CREATE PROCEDURE refresh_report_active_by_product_term_monthly()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_active_by_product_term_monthly;
  insert into report_active_by_product_term_monthly select * from v_report_active_by_product_term_monthly;
COMMIT;

END;
//
DELIMITER ;
