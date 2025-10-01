create table report_subscribers_vs_non_subscribers as select * from v_report_subscribers_vs_non_subscribers limit 0;

drop procedure if exists refresh_report_subscribers_vs_non_subscribers;
DELIMITER //
CREATE PROCEDURE refresh_report_subscribers_vs_non_subscribers()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_subscribers_vs_non_subscribers;
  insert into report_subscribers_vs_non_subscribers select * from v_report_subscribers_vs_non_subscribers;
COMMIT;

END;
//
DELIMITER ;
