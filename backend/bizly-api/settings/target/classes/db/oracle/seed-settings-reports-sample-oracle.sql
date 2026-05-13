-- =============================================================================
-- Seed: Customers with Transactions / Users Audit / Products
--
-- What this script does:
--   1. Upserts three saved queries in UM.SETTINGS_QUERY_DEF (Query Builder).
--   2. Upserts the matching reports in UM.SETTINGS_REPORT (Report Builder) and
--      links each one to its query via QUERY_DEF_ID.
--
-- It is fully idempotent: re-running refreshes the SQL text, filters JSON,
-- columns JSON and metadata in place. Match is by stable identifiers:
--   - queries  → name (case-insensitive)
--   - reports  → code (slug)
--
-- Filter binding contract (matches ReportFilterConfig on the backend):
--   - DATE_RANGE → fromParamName / toParamName
--   - everything else → paramName
--   - param names must match the :placeholders inside the linked SQL
--
-- Run as the UM user (or any account with INSERT/UPDATE on UM.* tables).
-- =============================================================================

SET DEFINE OFF;

------------------------------------------------------------
-- 1) Customers with Transactions
------------------------------------------------------------
DECLARE
  v_query_id   NUMBER;
  v_query_name VARCHAR2(200) := 'Customers with Transactions';
  v_code       VARCHAR2(120) := 'customers-with-transactions';

  v_sql        VARCHAR2(32767) := q'~
SELECT
  c.id                                   AS CUSTOMER_ID,
  c.full_name                            AS CUSTOMER_NAME,
  c.email                                AS EMAIL,
  c.mobile_number                        AS MOBILE,
  c.country                              AS COUNTRY,
  c.customer_status                      AS STATUS,
  COUNT(s.id)                            AS TX_COUNT,
  NVL(SUM(s.total_amount), 0)            AS TX_TOTAL,
  MIN(s.created_at)                      AS FIRST_TX_AT,
  MAX(s.created_at)                      AS LAST_TX_AT
FROM um.kyc_customer c
JOIN um.pm_customer_sale s
  ON s.customer_id = c.id
WHERE (:fromDate     IS NULL OR s.created_at >= :fromDate)
  AND (:toDate       IS NULL OR s.created_at <  :toDate)
  AND (:customerName IS NULL OR UPPER(c.full_name) LIKE '%' || UPPER(:customerName) || '%')
  AND (:saleStatus   IS NULL OR s.status = :saleStatus)
GROUP BY
  c.id, c.full_name, c.email, c.mobile_number, c.country, c.customer_status
~';

  v_filters    VARCHAR2(32767) := q'~[
  {"key":"period","label":"Sale period","type":"DATE_RANGE","fromParamName":"fromDate","toParamName":"toDate"},
  {"key":"customerName","label":"Customer name","type":"TEXT","paramName":"customerName","placeholder":"e.g. John"},
  {"key":"saleStatus","label":"Sale status","type":"SELECT","paramName":"saleStatus","options":[
    {"value":"PAID","label":"Paid"},
    {"value":"PENDING","label":"Pending"},
    {"value":"CANCELLED","label":"Cancelled"}
  ]}
]~';

  v_columns    VARCHAR2(32767) := q'~[
  {"key":"CUSTOMER_ID","label":"Customer Id","type":"NUMBER","sortable":true,"visible":true},
  {"key":"CUSTOMER_NAME","label":"Customer Name","type":"STRING","sortable":true,"visible":true},
  {"key":"EMAIL","label":"Email","type":"STRING","sortable":true,"visible":true},
  {"key":"MOBILE","label":"Mobile","type":"STRING","sortable":true,"visible":true},
  {"key":"COUNTRY","label":"Country","type":"STRING","sortable":true,"visible":true},
  {"key":"STATUS","label":"Status","type":"STRING","sortable":true,"visible":true},
  {"key":"TX_COUNT","label":"Tx Count","type":"NUMBER","sortable":true,"visible":true},
  {"key":"TX_TOTAL","label":"Tx Total","type":"MONEY","sortable":true,"visible":true},
  {"key":"FIRST_TX_AT","label":"First Tx At","type":"DATETIME","sortable":true,"visible":true},
  {"key":"LAST_TX_AT","label":"Last Tx At","type":"DATETIME","sortable":true,"visible":true}
]~';
BEGIN
  SELECT MAX(id) INTO v_query_id
    FROM um.settings_query_def
   WHERE UPPER(TRIM(name)) = UPPER(v_query_name);

  IF v_query_id IS NULL THEN
    INSERT INTO um.settings_query_def
      (name, description, sql_text, created_at, updated_at, created_by)
    VALUES
      (v_query_name,
       'One row per customer who had at least one sale in the selected period, with tx count, total, first and last tx dates.',
       v_sql, SYSTIMESTAMP, SYSTIMESTAMP, 'system')
    RETURNING id INTO v_query_id;
  ELSE
    UPDATE um.settings_query_def
       SET description =
             'One row per customer who had at least one sale in the selected period, with tx count, total, first and last tx dates.',
           sql_text    = v_sql,
           updated_at  = SYSTIMESTAMP
     WHERE id = v_query_id;
  END IF;

  MERGE INTO um.settings_report r
  USING (SELECT v_code AS code FROM dual) src
     ON (r.code = src.code)
   WHEN MATCHED THEN
     UPDATE SET
       name             = 'Customers with Transactions',
       description      = 'Customers with their sales totals (count, sum, first/last) for the selected period.',
       icon             = 'users',
       status           = 'ACTIVE',
       query_def_id     = v_query_id,
       filters_json     = v_filters,
       columns_json     = v_columns,
       default_sort_key = 'LAST_TX_AT',
       default_sort_dir = 'DESC',
       sort_order       = 10,
       updated_at       = SYSTIMESTAMP,
       updated_by       = 'system'
   WHEN NOT MATCHED THEN
     INSERT (code, name, description, icon, status, query_def_id,
             filters_json, columns_json, default_sort_key, default_sort_dir,
             sort_order, created_at, updated_at, created_by, updated_by)
     VALUES (src.code, 'Customers with Transactions',
             'Customers with their sales totals (count, sum, first/last) for the selected period.',
             'users', 'ACTIVE', v_query_id,
             v_filters, v_columns, 'LAST_TX_AT', 'DESC',
             10, SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
END;
/

------------------------------------------------------------
-- 2) Users Audit
------------------------------------------------------------
DECLARE
  v_query_id   NUMBER;
  v_query_name VARCHAR2(200) := 'Users Audit';
  v_code       VARCHAR2(120) := 'users-audit';

  v_sql        VARCHAR2(32767) := q'~
SELECT
  a.id            AS LOG_ID,
  a.username      AS USERNAME,
  a.action_code   AS ACTION,
  a.resource_type AS RESOURCE_TYPE,
  a.resource_id   AS RESOURCE_ID,
  a.http_method   AS HTTP_METHOD,
  a.request_path  AS REQUEST_PATH,
  a.ip_address    AS IP_ADDRESS,
  a.session_id    AS SESSION_ID,
  a.created_at    AS CREATED_AT
FROM um.um_audit_log a
WHERE (:fromDate     IS NULL OR a.created_at >= :fromDate)
  AND (:toDate       IS NULL OR a.created_at <  :toDate)
  AND (:username     IS NULL OR UPPER(a.username) LIKE '%' || UPPER(:username) || '%')
  AND (:actionCode   IS NULL OR UPPER(a.action_code) LIKE '%' || UPPER(:actionCode) || '%')
  AND (:resourceType IS NULL OR a.resource_type = :resourceType)
  AND (:httpMethod   IS NULL OR a.http_method = :httpMethod)
~';

  v_filters    VARCHAR2(32767) := q'~[
  {"key":"period","label":"Period","type":"DATE_RANGE","fromParamName":"fromDate","toParamName":"toDate"},
  {"key":"username","label":"Username","type":"USER","paramName":"username","placeholder":"contains…"},
  {"key":"actionCode","label":"Action code","type":"TEXT","paramName":"actionCode","placeholder":"e.g. LOGIN"},
  {"key":"resourceType","label":"Resource type","type":"TEXT","paramName":"resourceType"},
  {"key":"httpMethod","label":"HTTP method","type":"SELECT","paramName":"httpMethod","options":[
    {"value":"GET","label":"GET"},
    {"value":"POST","label":"POST"},
    {"value":"PUT","label":"PUT"},
    {"value":"DELETE","label":"DELETE"},
    {"value":"PATCH","label":"PATCH"}
  ]}
]~';

  v_columns    VARCHAR2(32767) := q'~[
  {"key":"LOG_ID","label":"Log Id","type":"NUMBER","sortable":true,"visible":true},
  {"key":"USERNAME","label":"Username","type":"STRING","sortable":true,"visible":true},
  {"key":"ACTION","label":"Action","type":"STRING","sortable":true,"visible":true},
  {"key":"RESOURCE_TYPE","label":"Resource Type","type":"STRING","sortable":true,"visible":true},
  {"key":"RESOURCE_ID","label":"Resource Id","type":"STRING","sortable":true,"visible":true},
  {"key":"HTTP_METHOD","label":"Http Method","type":"STRING","sortable":true,"visible":true},
  {"key":"REQUEST_PATH","label":"Request Path","type":"STRING","sortable":true,"visible":true},
  {"key":"IP_ADDRESS","label":"Ip Address","type":"STRING","sortable":true,"visible":true},
  {"key":"SESSION_ID","label":"Session Id","type":"STRING","sortable":false,"visible":false},
  {"key":"CREATED_AT","label":"Created At","type":"DATETIME","sortable":true,"visible":true}
]~';
BEGIN
  SELECT MAX(id) INTO v_query_id
    FROM um.settings_query_def
   WHERE UPPER(TRIM(name)) = UPPER(v_query_name);

  IF v_query_id IS NULL THEN
    INSERT INTO um.settings_query_def
      (name, description, sql_text, created_at, updated_at, created_by)
    VALUES
      (v_query_name,
       'Audit-log entries with optional filters by user, action, resource and method.',
       v_sql, SYSTIMESTAMP, SYSTIMESTAMP, 'system')
    RETURNING id INTO v_query_id;
  ELSE
    UPDATE um.settings_query_def
       SET description = 'Audit-log entries with optional filters by user, action, resource and method.',
           sql_text    = v_sql,
           updated_at  = SYSTIMESTAMP
     WHERE id = v_query_id;
  END IF;

  MERGE INTO um.settings_report r
  USING (SELECT v_code AS code FROM dual) src
     ON (r.code = src.code)
   WHEN MATCHED THEN
     UPDATE SET
       name             = 'Users Audit',
       description      = 'Full audit log with date / user / action / method filters.',
       icon             = 'shield-lock',
       status           = 'ACTIVE',
       query_def_id     = v_query_id,
       filters_json     = v_filters,
       columns_json     = v_columns,
       default_sort_key = 'CREATED_AT',
       default_sort_dir = 'DESC',
       sort_order       = 20,
       updated_at       = SYSTIMESTAMP,
       updated_by       = 'system'
   WHEN NOT MATCHED THEN
     INSERT (code, name, description, icon, status, query_def_id,
             filters_json, columns_json, default_sort_key, default_sort_dir,
             sort_order, created_at, updated_at, created_by, updated_by)
     VALUES (src.code, 'Users Audit',
             'Full audit log with date / user / action / method filters.',
             'shield-lock', 'ACTIVE', v_query_id,
             v_filters, v_columns, 'CREATED_AT', 'DESC',
             20, SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
END;
/

------------------------------------------------------------
-- 3) Products
------------------------------------------------------------
DECLARE
  v_query_id   NUMBER;
  v_query_name VARCHAR2(200) := 'Products';
  v_code       VARCHAR2(120) := 'products';

  v_sql        VARCHAR2(32767) := q'~
SELECT
  p.id             AS PRODUCT_ID,
  p.name           AS PRODUCT_NAME,
  p.price          AS PRICE,
  p.stock_quantity AS STOCK_QUANTITY,
  p.created_at     AS CREATED_AT
FROM um.pm_product p
WHERE (:fromDate    IS NULL OR p.created_at >= :fromDate)
  AND (:toDate      IS NULL OR p.created_at <  :toDate)
  AND (:productName IS NULL OR UPPER(p.name) LIKE '%' || UPPER(:productName) || '%')
  AND (:minPrice    IS NULL OR p.price >= :minPrice)
  AND (:maxPrice    IS NULL OR p.price <= :maxPrice)
~';

  v_filters    VARCHAR2(32767) := q'~[
  {"key":"period","label":"Created period","type":"DATE_RANGE","fromParamName":"fromDate","toParamName":"toDate"},
  {"key":"productName","label":"Product name","type":"TEXT","paramName":"productName","placeholder":"contains…"},
  {"key":"minPrice","label":"Min price","type":"NUMBER","paramName":"minPrice"},
  {"key":"maxPrice","label":"Max price","type":"NUMBER","paramName":"maxPrice"}
]~';

  v_columns    VARCHAR2(32767) := q'~[
  {"key":"PRODUCT_ID","label":"Product Id","type":"NUMBER","sortable":true,"visible":true},
  {"key":"PRODUCT_NAME","label":"Product Name","type":"STRING","sortable":true,"visible":true},
  {"key":"PRICE","label":"Price","type":"MONEY","sortable":true,"visible":true},
  {"key":"STOCK_QUANTITY","label":"Stock Quantity","type":"NUMBER","sortable":true,"visible":true},
  {"key":"CREATED_AT","label":"Created At","type":"DATETIME","sortable":true,"visible":true}
]~';
BEGIN
  SELECT MAX(id) INTO v_query_id
    FROM um.settings_query_def
   WHERE UPPER(TRIM(name)) = UPPER(v_query_name);

  IF v_query_id IS NULL THEN
    INSERT INTO um.settings_query_def
      (name, description, sql_text, created_at, updated_at, created_by)
    VALUES
      (v_query_name,
       'Product catalog with stock and price, filtered by creation period and price range.',
       v_sql, SYSTIMESTAMP, SYSTIMESTAMP, 'system')
    RETURNING id INTO v_query_id;
  ELSE
    UPDATE um.settings_query_def
       SET description = 'Product catalog with stock and price, filtered by creation period and price range.',
           sql_text    = v_sql,
           updated_at  = SYSTIMESTAMP
     WHERE id = v_query_id;
  END IF;

  MERGE INTO um.settings_report r
  USING (SELECT v_code AS code FROM dual) src
     ON (r.code = src.code)
   WHEN MATCHED THEN
     UPDATE SET
       name             = 'Products',
       description      = 'Product catalog with price + stock; filter by created period and price range.',
       icon             = 'building-store',
       status           = 'ACTIVE',
       query_def_id     = v_query_id,
       filters_json     = v_filters,
       columns_json     = v_columns,
       default_sort_key = 'CREATED_AT',
       default_sort_dir = 'DESC',
       sort_order       = 30,
       updated_at       = SYSTIMESTAMP,
       updated_by       = 'system'
   WHEN NOT MATCHED THEN
     INSERT (code, name, description, icon, status, query_def_id,
             filters_json, columns_json, default_sort_key, default_sort_dir,
             sort_order, created_at, updated_at, created_by, updated_by)
     VALUES (src.code, 'Products',
             'Product catalog with price + stock; filter by created period and price range.',
             'building-store', 'ACTIVE', v_query_id,
             v_filters, v_columns, 'CREATED_AT', 'DESC',
             30, SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
END;
/

COMMIT;

-- =============================================================================
-- Verify:
--   SELECT id, name FROM um.settings_query_def
--    WHERE name IN ('Customers with Transactions','Users Audit','Products');
--
--   SELECT id, code, name, status, query_def_id, default_sort_key
--     FROM um.settings_report
--    WHERE code IN ('customers-with-transactions','users-audit','products')
--    ORDER BY sort_order;
-- =============================================================================
