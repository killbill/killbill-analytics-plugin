create table report_new_subscriptions_daily as select * from v_report_new_subscriptions_daily limit 0;

drop procedure if exists refresh_report_new_subscriptions_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_report_new_subscriptions_daily()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_new_subscriptions_daily;
  insert into report_new_subscriptions_daily select * from v_report_new_subscriptions_daily;
COMMIT;

END;
//
DELIMITER ;
