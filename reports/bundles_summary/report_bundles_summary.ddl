create table report_bundles_summary as select * from v_report_bundles_summary limit 0;

drop procedure if exists refresh_report_bundles_summary;
DELIMITER //
CREATE PROCEDURE refresh_report_bundles_summary()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_bundles_summary;
  insert into report_bundles_summary select * from v_report_bundles_summary;
COMMIT;

END;
//
DELIMITER ;
