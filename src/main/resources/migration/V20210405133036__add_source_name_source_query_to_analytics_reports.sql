alter table analytics_reports modify source_table_name varchar(256) default null;
alter table analytics_reports add source_name varchar(256) default null after source_table_name;
alter table analytics_reports add source_query varchar(4096) default null after source_name;
