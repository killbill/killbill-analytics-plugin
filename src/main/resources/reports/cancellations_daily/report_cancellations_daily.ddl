create table report_cancellations_daily as select * from v_report_cancellations_daily limit 0;

drop procedure if exists refresh_report_cancellations_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_cancellations_daily()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_cancellations_daily;
  insert into report_cancellations_daily select * from v_report_cancellations_daily;
COMMIT;

END;
//
DELIMITER ;
