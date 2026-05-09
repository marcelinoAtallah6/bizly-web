-- KYC customer: move from generic detail rows (field_name/field_value) to columns on kyc_customer.
-- Schema: UM (matches com.kyc.common.DatabaseConstants.SCHEMA)
-- Review and run in SQL*Plus or SQL Developer as a user with DDL on UM.

-- 1) Add address and status columns to the main customer table
ALTER TABLE kyc_customer ADD (
  address_line1   VARCHAR2(255),
  address_line2   VARCHAR2(255),
  city            VARCHAR2(100),
  state_province  VARCHAR2(100),
  postal_code     VARCHAR2(20),
  country         VARCHAR2(100),
  customer_status VARCHAR2(20)
);

-- 2) Optional: one-time backfill of customer_status from the first detail row per customer
-- (Uncomment if you have legacy data in kyc_customer_detail and need to preserve status.)
/*
MERGE INTO kyc_customer c
USING (
  SELECT customer_id, MIN(id) AS first_detail_id
  FROM kyc_customer_detail
  GROUP BY customer_id
) m
ON (c.id = m.customer_id)
WHEN MATCHED THEN
  UPDATE SET c.customer_status = (
    SELECT d.customer_status
    FROM kyc_customer_detail d
    WHERE d.id = m.first_detail_id
  )
  WHERE c.customer_status IS NULL;
*/

-- 3) Drop the old detail table (removes field_name, field_value, etc.)
DROP TABLE kyc_customer_detail CASCADE CONSTRAINTS PURGE;

-- 4) Optional: drop the detail sequence if it exists and is no longer used
-- DROP SEQUENCE kyc_customer_detail_seq;

COMMIT;
