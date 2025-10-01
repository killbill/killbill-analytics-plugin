create table report_trial_to_no_trial_conversions_daily as select * from v_report_trial_to_no_trial_conversions_daily limit 0;

drop procedure if exists refresh_report_trial_to_no_trial_conversions_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_trial_to_no_trial_conversions_daily()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_trial_to_no_trial_conversions_daily;
  insert into report_trial_to_no_trial_conversions_daily select * from v_report_trial_to_no_trial_conversions_daily;
COMMIT;

END;
//
DELIMITER ;
