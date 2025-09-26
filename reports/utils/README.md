# Insert Monthly Currency Conversion Rates

This procedure inserts the monthly currency conversion rates (AUD, BRL, EUR, GBP, MXN → USD) into the `analytics_currency_conversion` table.
It validates the input month/year, checks for duplicate or missing previous entries, ensures rates don’t deviate beyond a threshold, updates prior rows, and inserts the new month’s rates.

## Initialization

Before executing the procedure, the following initialization script needs to be run:

````
insert into analytics_currency_conversion values (1, 'AUD', date_sub(str_to_date(concat(cast(year(now()) as char(4)),'-',cast(month(now()) as char(2)),'-01'),'%Y-%m-%d'), interval 1 month), '2020-01-01', 0.77, 'USD');
insert into analytics_currency_conversion values (2, 'BRL', date_sub(str_to_date(concat(cast(year(now()) as char(4)),'-',cast(month(now()) as char(2)),'-01'),'%Y-%m-%d'), interval 1 month), '2020-01-01', 0.31, 'USD');
insert into analytics_currency_conversion values (3, 'EUR', date_sub(str_to_date(concat(cast(year(now()) as char(4)),'-',cast(month(now()) as char(2)),'-01'),'%Y-%m-%d'), interval 1 month), '2020-01-01', 1.12, 'USD');
insert into analytics_currency_conversion values (4, 'GBP', date_sub(str_to_date(concat(cast(year(now()) as char(4)),'-',cast(month(now()) as char(2)),'-01'),'%Y-%m-%d'), interval 1 month), '2020-01-01', 1.29, 'USD');
insert into analytics_currency_conversion values (5, 'MXN', date_sub(str_to_date(concat(cast(year(now()) as char(4)),'-',cast(month(now()) as char(2)),'-01'),'%Y-%m-%d'), interval 1 month), '2020-01-01', 0.052, 'USD');
````

## Executing The Procedure

The stored procedure can be executed as follows:

````
CALL updateAnalyticsCurrConvMonth(
   9,   -- month = September
   2025, -- year
   0.78, -- AUD
   0.30, -- BRL
   1.13, -- EUR
   1.28, -- GBP
   0.051 -- MXN
);
````

## How it works

- The procedure verifies that the previous month has entries for all five currencies. 
- It ensures the current month’s entries don’t already exist. 
- Rates are validated to be positive and within a 10% deviation from the previous month. 
- The end_date of previous month entries is updated to the start of the new month. 
- New records are inserted for the current month with `end_date = 2020-01-01`.

Here is an example. 

Suppose the `analytics_currency_conversion` table has the conversion rates for August 2025:

| Currency | Start Date | End Date   | Reference Rate | Reference Currency |
|----------|------------|------------|----------------|--------------------|
| AUD      | 2025-08-01 | 2020-01-01 | 0.77           | USD                |
| BRL      | 2025-08-01 | 2020-01-01 | 0.31           | USD                |
| EUR      | 2025-08-01 | 2020-01-01 | 1.12           | USD                |
| GBP      | 2025-08-01 | 2020-01-01 | 1.29           | USD                |
| MXN      | 2025-08-01 | 2020-01-01 | 0.052          | USD                |
Notice how all `end_date = 2020-01-01`.
This is a placeholder meaning “open-ended” (valid until a new record replaces it).

2. After calling the procedure for September 2025 as mentioned above, the table now looks as follows:

````
| Currency | Start Date | End Date   | Reference Rate | Reference Currency |
|----------|------------|------------|----------------|--------------------|
| AUD      | 2025-08-01 | 2025-09-01 | 0.77           | USD                |
| BRL      | 2025-08-01 | 2025-09-01 | 0.31           | USD                |
| EUR      | 2025-08-01 | 2025-09-01 | 1.12           | USD                |
| GBP      | 2025-08-01 | 2025-09-01 | 1.29           | USD                |
| MXN      | 2025-08-01 | 2025-09-01 | 0.052          | USD                |
| AUD      | 2025-09-01 | 2020-01-01 | 0.78           | USD                |
| BRL      | 2025-09-01 | 2020-01-01 | 0.30           | USD                |
| EUR      | 2025-09-01 | 2020-01-01 | 1.13           | USD                |
| GBP      | 2025-09-01 | 2020-01-01 | 1.28           | USD                |
| MXN      | 2025-09-01 | 2020-01-01 | 0.051          | USD                |
````

