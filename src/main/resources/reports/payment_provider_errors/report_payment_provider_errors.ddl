create table report_payment_provider_errors_sub2 as select * from v_report_payment_provider_errors_sub2 limit 0;
create table report_payment_provider_errors as select * from v_report_payment_provider_errors limit 0;

drop procedure if exists refresh_report_payment_provider_errors;
DELIMITER //
CREATE PROCEDURE refresh_report_payment_provider_errors()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

START TRANSACTION;
  delete from report_payment_provider_errors_sub2;
  insert into report_payment_provider_errors_sub2 select * from v_report_payment_provider_errors_sub2;

  delete from report_payment_provider_errors;
  insert into report_payment_provider_errors
    select
      tenant_record_id
    , day
    , currency
    , plugin_name
    , plugin_gateway_error
    , count
    from report_payment_provider_errors_sub2 sub2
    where (
      select count(*) from report_payment_provider_errors_sub2 as sub21
      where 1=1
        and sub21.tenant_record_id = sub2.tenant_record_id
        and sub21.day = sub2.day
        and sub21.currency = sub2.currency
        and sub21.plugin_name = sub2.plugin_name
        and sub21.count >= sub2.count
    ) <= 3
  ;
COMMIT;

END;
//
DELIMITER ;
