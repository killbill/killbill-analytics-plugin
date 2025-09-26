create table report_trial_starts_count_daily as select * from v_report_trial_starts_count_daily limit 0;

drop procedure if exists refresh_report_trial_starts_count_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_trial_starts_count_daily()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_trial_starts_count_daily;
  insert into report_trial_starts_count_daily select * from v_report_trial_starts_count_daily;
COMMIT;

END;
//
DELIMITER ;
