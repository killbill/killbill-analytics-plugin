drop procedure if exists updMonthAnalyticsCurrConv;
DELIMITER //
CREATE PROCEDURE updMonthAnalyticsCurrConv(p_month int, p_year int, p_aud_rate decimal(10,4), p_brl_rate decimal(10,4), p_eur_rate decimal(10,4), p_gbp_rate decimal(10,4), p_mxn_rate decimal(10,4))
BEGIN

    /***********************
     *
     *  Convenience method to set the currency conversion rates for the month
     *
     *  Kevin Postlewaite, March 2014
     *
     ***********************/


    DECLARE specialty CONDITION FOR SQLSTATE '45000';
    DECLARE v_month_start date;    
    DECLARE v_test_dup_month decimal(10,4);
    DECLARE v_test_prev_month int;
    DECLARE v_old_aud_rate decimal(10,4);
    DECLARE v_old_brl_rate decimal(10,4);
    DECLARE v_old_eur_rate decimal(10,4);
    DECLARE v_old_gbp_rate decimal(10,4);
    DECLARE v_old_mxn_rate decimal(10,4);
    DECLARE v_conv_rate_test_threshhold decimal(10,4);
    DECLARE v_foreign_currency_count int;
    
    set v_conv_rate_test_threshhold=.1;
    set v_foreign_currency_count=5;
    
    -- Validate the input   
    if p_month<1 or p_month>12 or p_month is null then
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid month parameter';
    elseif p_year<1970 or p_year>2050 or p_year is null then
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid year parameter'; 
    elseif p_aud_rate is null or p_brl_rate is null or p_eur_rate is null or p_gbp_rate is null or p_mxn_rate is null
       or p_aud_rate < 0 or p_brl_rate < 0 or p_eur_rate < 0 or p_gbp_rate < 0 or p_mxn_rate < 0 then
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'All currency rates must be specified and must be positive';
    end if;
    
    -- Build the period start date, first of the month
    set v_month_start=str_to_date(concat(cast(p_year as char(4)),'-',cast(p_month as char(2)),'-01'),'%Y-%m-%d');
    
    -- Check to make sure we have all foreign currencies entered for previous month
    select count(1)
    into v_test_prev_month
    from analytics_currency_conversion
    where 1=1
      and start_date=date_sub(v_month_start,interval 1 month);
    
    if v_test_prev_month!=v_foreign_currency_count then
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Did not find correct number of previous currency conversion entries';
    end if;
    
    -- Check to make sure we haven't already entered the month's entries
    select sum(reference_rate) into v_test_dup_month from analytics_currency_conversion where start_date = v_month_start;
  
    if v_test_dup_month is not null then
        SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Conversion rates for this month already exist';
    end if;

    -- Check to make sure that the new entries are close to the old entries
    select
      sum(case when currency='AUD' then reference_rate else 0 end) old_aud_rate
    , sum(case when currency='BRL' then reference_rate else 0 end) old_brl_rate
    , sum(case when currency='EUR' then reference_rate else 0 end) old_eur_rate
    , sum(case when currency='GBP' then reference_rate else 0 end) old_gbp_rate
    , sum(case when currency='MXN' then reference_rate else 0 end) old_mxn_rate             
    into v_old_aud_rate,v_old_brl_rate,v_old_eur_rate,v_old_gbp_rate,v_old_mxn_rate
    from
      analytics_currency_conversion
    where 1=1
      and end_date = v_month_start;
        
    if abs(v_old_aud_rate-p_aud_rate)/v_old_aud_rate >v_conv_rate_test_threshhold then
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Not Allowed: New AUD rate differs from previous rate by more than allowed threshhold';

    elseif abs(v_old_brl_rate-p_brl_rate)/v_old_aud_rate >v_conv_rate_test_threshhold then
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Not Allowed: New BRL rate differs from previous rate by more than allowed threshhold';

    elseif abs(v_old_eur_rate-p_eur_rate)/v_old_aud_rate >v_conv_rate_test_threshhold then
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Not Allowed: New EUR rate differs from previous rate by more than allowed threshhold';

    elseif abs(v_old_gbp_rate-p_gbp_rate)/v_old_aud_rate >v_conv_rate_test_threshhold then
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Not Allowed: New GBP rate differs from previous rate by more than allowed threshhold';

    elseif abs(v_old_mxn_rate-p_mxn_rate)/v_old_aud_rate >v_conv_rate_test_threshhold then
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Not Allowed: New MXN rate differs from previous rate by more than allowed threshhold';
    end if;  
    
    /* 
    Or we could insert, run the following and rollback if max>.1:
    DECLARE v_max_rate_change DECIMAL(10,4);
    
    select max(abs((a.reference_rate-b.reference_rate)/a.reference_rate))
    into v_max_rate_change
    from 
      analytics_currency_conversion a
      join analytics_currency_conversion b on b.start_date=a.end_date and a.currency=b.currency
    where 1=1
      and a.end_date=date_sub(v_month_start,interval 1 month); */

    -- The previous entries will end in the future so move it so it lines up with new entries
    update analytics_currency_conversion
    set end_date=v_month_start
    where end_date='2020-01-01';

    -- Insert the new entries
    insert into analytics_currency_conversion (currency, start_date,end_date,reference_rate,reference_currency)
    values ('AUD', v_month_start,'2020-01-01',p_aud_rate,'USD');

    insert into analytics_currency_conversion (currency, start_date,end_date,reference_rate,reference_currency)
    values ('BRL', v_month_start,'2020-01-01',p_brl_rate,'USD');
    
    insert into analytics_currency_conversion (currency, start_date,end_date,reference_rate,reference_currency)
    values ('EUR', v_month_start,'2020-01-01',p_eur_rate,'USD');
    
    insert into analytics_currency_conversion (currency, start_date,end_date,reference_rate,reference_currency)
    values ('GBP', v_month_start,'2020-01-01',p_gbp_rate,'USD');
    
    insert into analytics_currency_conversion (currency, start_date,end_date,reference_rate,reference_currency)
    values ('MXN', v_month_start,'2020-01-01',p_mxn_rate,'USD');

    commit;
    
END;
//
DELIMITER ;

