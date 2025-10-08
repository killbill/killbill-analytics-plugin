create table report_churn_percent as select tenant_record_id
   , month day, billing_period, churn_pct count from v_report_churn_percent_and_total_usd limit 0;

drop procedure if exists refresh_report_churn_percent;
DELIMITER //
CREATE PROCEDURE refresh_report_churn_percent()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_churn_percent;
  insert into report_churn_percent select tenant_record_id
                                              , month day, billing_period, churn_pct count from v_report_churn_percent_and_total_usd;
COMMIT;

END;
//
DELIMITER ;
