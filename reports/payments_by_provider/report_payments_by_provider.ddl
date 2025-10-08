create table report_payments_by_provider as select * from v_report_payments_by_provider limit 0;

drop procedure if exists refresh_report_payments_by_provider;
DELIMITER //
CREATE PROCEDURE refresh_report_payments_by_provider()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
insert into report_payments_by_provider select * from v_report_payments_by_provider;

END;
//
DELIMITER ;
