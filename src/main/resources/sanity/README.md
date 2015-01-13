These sanity queries verify that the data in the Analytics tables are in sync with the core Kill Bill tables.

* [account_record_id_sanity.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/sanity/account_record_id_sanity.sql): check the account_id/account_record_id mapping in each table is correct
* [sanity.sql](https://github.com/killbill/killbill-analytics-plugin/blob/master/src/main/resources/sanity/sanity.sql): check invariants to make sure the Analytics tables are self-consistent
