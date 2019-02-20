create table report_active_by_product_term_monthly as select * from v_report_active_by_product_term_monthly limit 0;

drop procedure if exists refresh_report_active_by_product_term_monthly;
DELIMITER //
CREATE PROCEDURE refresh_report_active_by_product_term_monthly()
BEGIN

DECLARE EXIT HANDLER FOR SQLEXCEPTION ROLLBACK;
DECLARE EXIT HANDLER FOR SQLWARNING ROLLBACK;

SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
START TRANSACTION;
  delete from report_active_by_product_term_monthly;
  insert into report_active_by_product_term_monthly
  select
    x.tenant_record_id
  , cal.d day
  , x.product_name
  , x.billing_period
  , x.count
  from calendar cal
  join (
    select
      tenant_record_id
    , last_day(day) day
    , product_name
    , billing_period
    , count
    from
      v_report_active_by_product_term_monthly
    where 1=1
  ) x on last_day(cal.d) = x.day
  where 1=1
  ;
COMMIT;

END;
//
DELIMITER ;
