create table report_accounts_summary as select * from v_report_accounts_summary limit 0;

drop procedure if exists refresh_report_accounts_summary;
DELIMITER //
CREATE PROCEDURE refresh_report_accounts_summary()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_accounts_summary;
  insert into report_accounts_summary select * from v_report_accounts_summary;
COMMIT;

END;
//
DELIMITER ;
