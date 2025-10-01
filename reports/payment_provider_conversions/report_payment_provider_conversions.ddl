create table report_payment_provider_conversion as select * from v_report_payment_provider_conversion limit 0;

drop procedure if exists refresh_report_payment_provider_conversion;
DELIMITER //
CREATE PROCEDURE refresh_report_payment_provider_conversion()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
insert into report_payment_provider_conversion select * from v_report_payment_provider_conversion;

END;
//
DELIMITER ;
