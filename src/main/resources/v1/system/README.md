These system queries help verify the health of Kill Bill.

* [system_report_control_tag_no_test.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/system/system_report_control_tag_no_test.sql): for each system tag, count the number of non-test accounts
* [system_report_notifications_per_queue_name.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/system/system_report_notifications_per_queue_name.sql): for each notification queue and date, list the number of *AVAILABLE* notifications
* [system_report_notifications_per_queue_name_late.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/system/system_report_notifications_per_queue_name_late.sql): for each notification queue, count the number of late notifications
* [system_report_payments.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/system/system_report_payments.sql): count the number of payments in each state
* [system_report_payments_per_day.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/system/system_report_payments_per_day.sql): count the number of payments in each state per day
