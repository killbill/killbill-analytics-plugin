create table report_chargebacks_daily as select * from v_report_chargebacks_daily limit 0;

drop procedure if exists refresh_report_chargebacks_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_chargebacks_daily()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_chargebacks_daily;
  insert into report_chargebacks_daily select * from v_report_chargebacks_daily;
COMMIT;

END;
//
DELIMITER ;
