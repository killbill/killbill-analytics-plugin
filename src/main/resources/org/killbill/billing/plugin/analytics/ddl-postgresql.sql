/* We cannot use timestamp in MySQL because of the implicit TimeZone conversions it does behind the scenes */
DROP DOMAIN IF EXISTS datetime CASCADE;
CREATE DOMAIN datetime AS timestamp without time zone;

CREATE OR REPLACE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION last_insert_id() RETURNS BIGINT AS $$
    DECLARE
        result BIGINT;
    BEGIN
        SELECT lastval() INTO result;
        RETURN result;
    EXCEPTION WHEN OTHERS THEN
        SELECT NULL INTO result;
        RETURN result;
    END;
$$ LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION schema() RETURNS VARCHAR AS $$
    DECLARE
        result VARCHAR;
    BEGIN
        SELECT current_schema() INTO result;
        RETURN result;
    EXCEPTION WHEN OTHERS THEN
        SELECT NULL INTO result;
        RETURN result;
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION DATE_ADD(timestamp with time zone, interval) RETURNS timestamp with time zone AS $$
    BEGIN
        RETURN $1 + $2;
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION DATE_SUB(timestamp with time zone, interval) RETURNS timestamp with time zone AS $$
    BEGIN
        RETURN $1 - $2;
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION DATE_FORMAT(timestamp with time zone, text) RETURNS text AS $$
    DECLARE
        f text;
        r text[][] = ARRAY[['%Y','YYYY'],['%m','MM'],['%d','DD'],['%H','HH24'],['%i','MI'],['%S','SS'],['%k','FMHH24']];
        i int4;
    BEGIN
        f := $2;
        FOR i IN 1..array_upper(r, 1) LOOP
            f := replace(f, r[i][1], r[i][2]);
        END LOOP;
        RETURN to_char($1, f);
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION IFNULL(numeric, integer) RETURNS integer AS $$
    BEGIN
        IF ($1 IS NULL) THEN
            RETURN $2;
        END IF;
        RETURN $1::integer;
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION IFNULL(varchar, varchar) RETURNS varchar AS $$
    BEGIN
        IF ($1 IS NULL) THEN
            RETURN $2;
        END IF;
        RETURN $1;
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION UNIX_TIMESTAMP(timestamp with time zone) RETURNS int AS $$
    BEGIN
        RETURN date_part('epoch', $1)::int;
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION FROM_UNIXTIME(integer) RETURNS timestamp AS $$
    BEGIN
        RETURN to_timestamp($1)::timestamp AS result;
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION CONCAT(anyelement) RETURNS text AS $$
    BEGIN
        RETURN $1::text;
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION SYSDATE() RETURNS timestamp AS $$
    BEGIN
        RETURN now();
    END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION LAST_DAY(DATE) RETURNS DATE AS $$
    BEGIN
        RETURN (date_trunc('MONTH', $1) + INTERVAL '1 MONTH - 1 day')::DATE;
    END;
$$ LANGUAGE plpgsql IMMUTABLE;
