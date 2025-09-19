create table report_conversions_total_dollar_monthly as select * from v_report_conversions_total_dollar_monthly limit 0;

drop procedure if exists refresh_report_conversions_total_dollar_monthly;
DELIMITER //
CREATE PROCEDURE refresh_report_conversions_total_dollar_monthly()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_conversions_total_dollar_monthly;
  insert into report_conversions_total_dollar_monthly select * from v_report_conversions_total_dollar_monthly;
COMMIT;

END;
//
DELIMITER ;
