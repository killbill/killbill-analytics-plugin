create table report_mrr_daily as select * from v_report_mrr_daily limit 0;

drop procedure if exists refresh_report_mrr_daily;
DELIMITER //
CREATE PROCEDURE refresh_report_mrr_daily()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_mrr_daily;
  insert into report_mrr_daily select * from v_report_mrr_daily;
COMMIT;

END;
//
DELIMITER ;
