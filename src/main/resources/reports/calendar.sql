drop procedure if exists create_calendar;

delimiter //
create procedure create_calendar(calendar_from date, calendar_to date)
begin
  declare d date;
  set d = calendar_from;

  drop table if exists calendar;
  create table calendar(d date primary key);
  while d <= calendar_to do
    insert into calendar(d) values (d);
    set d = date_add(d, interval 1 day);
  end while;
end//

delimiter ;
call create_calendar(date_sub(date_format(now(), '%Y-%m-%d'), interval 5 year), date_add(date_format(now(), '%Y-%m-%d'), interval 10 year));
