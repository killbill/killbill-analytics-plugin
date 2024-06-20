create table report_payments_by_provider_history as select * from v_report_payments_by_provider limit 0;

drop procedure if exists refresh_report_payments_by_provider_history;
DELIMITER //
CREATE PROCEDURE refresh_report_payments_by_provider_history()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
insert into report_payments_by_provider_history select * from v_report_payments_by_provider;

END;
//
DELIMITER ;
