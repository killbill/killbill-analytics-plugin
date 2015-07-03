These are all views to support the Payments By Provider report which shows the number of payments by 
transaction state over different timeframes for each payment service provder (plugin).

These are subqueries implemented as separate views because MySQL does not permit subqueries within a view:
v_report_payments_by_provider_sub1.ddl
v_report_payments_by_provider_sub2.ddl
v_report_payments_by_provider_sub3.ddl

These views provide the same output, with labels either in English (_en) or Spanish (_es):
v_report_payments_by_provider_en.ddl
v_report_payments_by_provider_es.ddl
