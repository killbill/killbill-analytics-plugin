create table report_payment_provider_conversion_history as select * from v_report_payment_provider_conversion limit 0;
alter table report_payment_provider_conversion_history add column refresh_date datetime first;

drop procedure if exists refresh_report_payment_provider_conversion_history;
DELIMITER //
CREATE PROCEDURE refresh_report_payment_provider_conversion_history()
BEGIN
insert into report_payment_provider_conversion_history select sysdate(), v.* from v_report_payment_provider_conversion v;
END;
//
DELIMITER ;
