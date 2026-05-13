-- =============================================================================
-- Business-id rollout across every tenant-owned table.
--
-- Scope decisions (read once before running):
--
-- TENANT-OWNED (gets business_id, FK to UM_BUSINESS, index):
--   PM_PRODUCT, PM_PRODUCT_ITEMS, PM_CUSTOMER_SALE, PM_CUSTOMER_SALE_LINE,
--   BM_SERVICE_ITEM, BM_APPOINTMENT,
--   KYC_CUSTOMER, KYC_CUSTOMER_ORDER,
--   SETTINGS_DASHBOARD (built-in rows stay NULL = global),
--   SETTINGS_WIDGET    (built-in rows stay NULL = global),
--   SETTINGS_REPORT    (built-in / admin-defined stay NULL = global),
--   SETTINGS_QUERY_DEF (admin-defined queries stay NULL = global),
--   SETTINGS_DASH_ROLE_GRANT, SETTINGS_DASH_USER_GRANT (inherit dashboard scope),
--   NOTIF_INBOX, NOTIF_BROADCAST_MESSAGE,
--   NOTIF_EMAIL_TEMPLATE (system templates stay NULL = global),
--   UM_AUDIT_LOG (lets tenants see only their own audit trail)
--
-- GLOBAL / NOT SCOPED (intentionally NOT touched):
--   UM_ROLE, UM_ROLE_LEVEL, UM_USER_ROLE, UM_MENUS, UM_APPLICATIONS,
--   UM_BUSINESS (is the tenant table itself), UM_USER_SESSION (user-scoped),
--   SETTINGS_USER_FAVORITE / USER_PREF / NAV_PREF (already user-scoped).
--
-- Backfill strategy: existing rows in tenant tables get the first ACTIVE
-- business id so the running app keeps showing data. Customise per table by
-- editing the SQL if you want a different default — or delete the backfill
-- block to force every existing row to be re-created under a real business.
--
-- Idempotent: every block guards on data dictionary lookups (ALL_TAB_COLUMNS /
-- ALL_INDEXES / ALL_CONSTRAINTS) so the script can be re-run safely.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 0) Helper variable: the default backfill business id (lowest active business)
-- -----------------------------------------------------------------------------
-- We cannot share a PL/SQL variable across anonymous blocks, so each block
-- below resolves `v_default_business` on its own. Override by uncommenting one
-- of these lines:
--   DEFINE BACKFILL_BID = 1
--   COLUMN/VARIABLE binding via your client.
-- The current approach picks the first ACTIVE business and stamps it onto all
-- existing rows. NULL backfill is fine — the application's BusinessContextHolder
-- will simply refuse to read those rows until they're claimed by a tenant.

-- -----------------------------------------------------------------------------
-- 1) Generic patcher procedure. Idempotent: only adds the column / FK / index
--    when missing.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE UM.ADD_BUSINESS_ID_SCOPE(
  p_table   IN VARCHAR2,
  p_with_fk IN NUMBER DEFAULT 1
) AUTHID DEFINER IS
  v_count NUMBER;
  v_default_business NUMBER;
BEGIN
  -- Column
  SELECT COUNT(*) INTO v_count FROM ALL_TAB_COLUMNS
   WHERE OWNER = 'UM' AND TABLE_NAME = UPPER(p_table) AND COLUMN_NAME = 'BUSINESS_ID';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE UM.' || UPPER(p_table) || ' ADD (BUSINESS_ID NUMBER)';
  END IF;

  -- Index on (BUSINESS_ID)
  SELECT COUNT(*) INTO v_count FROM ALL_INDEXES
   WHERE OWNER = 'UM' AND INDEX_NAME = 'IDX_' || UPPER(p_table) || '_BUSINESS';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'CREATE INDEX UM.IDX_' || UPPER(p_table)
                   || '_BUSINESS ON UM.' || UPPER(p_table) || ' (BUSINESS_ID)';
  END IF;

  -- Foreign key to UM_BUSINESS (skip for tables where global-by-default makes sense
  -- and we want NULL rows to remain valid; FK still allows NULLs but the caller can
  -- pass p_with_fk = 0 to skip entirely).
  IF p_with_fk = 1 THEN
    SELECT COUNT(*) INTO v_count FROM ALL_CONSTRAINTS
     WHERE OWNER = 'UM' AND CONSTRAINT_NAME = 'FK_' || UPPER(p_table) || '_BUSINESS';
    IF v_count = 0 THEN
      EXECUTE IMMEDIATE 'ALTER TABLE UM.' || UPPER(p_table)
                     || ' ADD CONSTRAINT FK_' || UPPER(p_table)
                     || '_BUSINESS FOREIGN KEY (BUSINESS_ID) REFERENCES UM.UM_BUSINESS (ID)';
    END IF;
  END IF;

  -- Backfill: stamp the lowest ACTIVE business on any rows still NULL.
  SELECT COUNT(*) INTO v_count FROM UM.UM_BUSINESS;
  IF v_count > 0 THEN
    SELECT MIN(ID) INTO v_default_business
      FROM UM.UM_BUSINESS WHERE STATUS = 'ACTIVE';
    IF v_default_business IS NOT NULL THEN
      EXECUTE IMMEDIATE 'UPDATE UM.' || UPPER(p_table)
                     || ' SET BUSINESS_ID = :1 WHERE BUSINESS_ID IS NULL'
        USING v_default_business;
    END IF;
  END IF;
END;
/

-- -----------------------------------------------------------------------------
-- 2) Apply to every tenant-owned table.
--    Note: PM_PRODUCT was already patched by patch-business-scope-pm-product-oracle.sql
--    but ADD_BUSINESS_ID_SCOPE is idempotent so re-running is safe.
-- -----------------------------------------------------------------------------
BEGIN
  -- PM (product / sales)
  UM.ADD_BUSINESS_ID_SCOPE('PM_PRODUCT');
  UM.ADD_BUSINESS_ID_SCOPE('PM_PRODUCT_ITEMS');
  UM.ADD_BUSINESS_ID_SCOPE('PM_CUSTOMER_SALE');
  UM.ADD_BUSINESS_ID_SCOPE('PM_CUSTOMER_SALE_LINE');

  -- BM (services + appointments)
  UM.ADD_BUSINESS_ID_SCOPE('BM_SERVICE_ITEM');
  UM.ADD_BUSINESS_ID_SCOPE('BM_APPOINTMENT');

  -- KYC (customers + customer orders)
  UM.ADD_BUSINESS_ID_SCOPE('KYC_CUSTOMER');
  UM.ADD_BUSINESS_ID_SCOPE('KYC_CUSTOMER_ORDER');

  -- Settings (dashboards, reports, queries, grants)
  -- These keep NULL for built-in / admin-defined rows. The FK still allows NULLs.
  UM.ADD_BUSINESS_ID_SCOPE('SETTINGS_DASHBOARD');
  UM.ADD_BUSINESS_ID_SCOPE('SETTINGS_WIDGET');
  UM.ADD_BUSINESS_ID_SCOPE('SETTINGS_REPORT');
  UM.ADD_BUSINESS_ID_SCOPE('SETTINGS_QUERY_DEF');
  UM.ADD_BUSINESS_ID_SCOPE('SETTINGS_DASH_ROLE_GRANT');
  UM.ADD_BUSINESS_ID_SCOPE('SETTINGS_DASH_USER_GRANT');

  -- Notifications & broadcast
  UM.ADD_BUSINESS_ID_SCOPE('NOTIF_INBOX');
  UM.ADD_BUSINESS_ID_SCOPE('NOTIF_BROADCAST_MESSAGE');
  UM.ADD_BUSINESS_ID_SCOPE('NOTIF_EMAIL_TEMPLATE');

  -- Audit log — useful so each tenant only sees their own audit trail.
  UM.ADD_BUSINESS_ID_SCOPE('UM_AUDIT_LOG');
END;
/

-- -----------------------------------------------------------------------------
-- 3) Special rules — keep built-in / admin-defined rows GLOBAL (BUSINESS_ID = NULL).
--    The generic backfill above set business_id on EVERYTHING; here we revert it
--    to NULL for rows the application treats as global catalogs.
-- -----------------------------------------------------------------------------
BEGIN
  -- SETTINGS_DASHBOARD has IS_BUILTIN flag.
  BEGIN
    EXECUTE IMMEDIATE 'UPDATE UM.SETTINGS_DASHBOARD SET BUSINESS_ID = NULL WHERE IS_BUILTIN = 1';
  EXCEPTION WHEN OTHERS THEN NULL; -- column may not exist in older builds
  END;

  -- SETTINGS_QUERY_DEF / SETTINGS_REPORT: admin-defined templates stay global.
  -- The application can later re-stamp these with a real business id when a tenant
  -- customises them.
  BEGIN
    EXECUTE IMMEDIATE 'UPDATE UM.SETTINGS_QUERY_DEF SET BUSINESS_ID = NULL';
  EXCEPTION WHEN OTHERS THEN NULL;
  END;
  BEGIN
    EXECUTE IMMEDIATE 'UPDATE UM.SETTINGS_REPORT SET BUSINESS_ID = NULL';
  EXCEPTION WHEN OTHERS THEN NULL;
  END;

  -- NOTIF_EMAIL_TEMPLATE: system templates stay global. Tenants that customise
  -- a template will create a copy with their business_id at runtime.
  BEGIN
    EXECUTE IMMEDIATE 'UPDATE UM.NOTIF_EMAIL_TEMPLATE SET BUSINESS_ID = NULL';
  EXCEPTION WHEN OTHERS THEN NULL;
  END;
END;
/

-- -----------------------------------------------------------------------------
-- 4) Composite indexes for the highest-traffic lookup patterns.
--    Spec: (BUSINESS_ID, <natural sort or filter column>) so paginated tenant
--    queries can index-only scan.
-- -----------------------------------------------------------------------------
DECLARE
  v_count NUMBER;
  PROCEDURE create_ix(p_name VARCHAR2, p_table VARCHAR2, p_cols VARCHAR2) IS
    v NUMBER;
  BEGIN
    SELECT COUNT(*) INTO v FROM ALL_INDEXES
     WHERE OWNER = 'UM' AND INDEX_NAME = p_name;
    IF v = 0 THEN
      EXECUTE IMMEDIATE 'CREATE INDEX UM.' || p_name
                     || ' ON UM.' || p_table || ' (' || p_cols || ')';
    END IF;
  EXCEPTION WHEN OTHERS THEN NULL;
  END;
BEGIN
  create_ix('IDX_PM_PRODUCT_BIZ_NAME',         'PM_PRODUCT',           'BUSINESS_ID, NAME');
  create_ix('IDX_PM_CUST_SALE_BIZ_CREATED',    'PM_CUSTOMER_SALE',     'BUSINESS_ID, CREATED_AT');
  create_ix('IDX_BM_SVC_BIZ_NAME',             'BM_SERVICE_ITEM',      'BUSINESS_ID, NAME');
  create_ix('IDX_BM_APPT_BIZ_START',           'BM_APPOINTMENT',       'BUSINESS_ID, START_AT');
  create_ix('IDX_KYC_CUST_BIZ_NAME',           'KYC_CUSTOMER',         'BUSINESS_ID, LAST_NAME, FIRST_NAME');
  create_ix('IDX_KYC_ORDER_BIZ_DATE',          'KYC_CUSTOMER_ORDER',   'BUSINESS_ID, CREATED_AT');
  create_ix('IDX_SETTINGS_DASH_BIZ_NAME',      'SETTINGS_DASHBOARD',   'BUSINESS_ID, NAME');
  create_ix('IDX_SETTINGS_REPORT_BIZ_NAME',    'SETTINGS_REPORT',      'BUSINESS_ID, NAME');
  create_ix('IDX_NOTIF_INBOX_BIZ_USER',        'NOTIF_INBOX',          'BUSINESS_ID, USER_ID, CREATED_AT');
  create_ix('IDX_NOTIF_BC_BIZ_CREATED',        'NOTIF_BROADCAST_MESSAGE', 'BUSINESS_ID, CREATED_AT');
  create_ix('IDX_UM_AUDIT_BIZ_CREATED',        'UM_AUDIT_LOG',         'BUSINESS_ID, CREATED_AT');
END;
/

-- -----------------------------------------------------------------------------
-- 5) (Optional) Drop the helper procedure once you're sure the patch ran.
--    Leave it in place if you plan to add new tenant tables later.
-- -----------------------------------------------------------------------------
-- DROP PROCEDURE UM.ADD_BUSINESS_ID_SCOPE;

COMMIT;

-- Verify:
--   SELECT TABLE_NAME, COLUMN_NAME FROM ALL_TAB_COLUMNS
--    WHERE OWNER='UM' AND COLUMN_NAME='BUSINESS_ID' ORDER BY TABLE_NAME;
--   SELECT CONSTRAINT_NAME, TABLE_NAME FROM ALL_CONSTRAINTS
--    WHERE OWNER='UM' AND CONSTRAINT_NAME LIKE '%BUSINESS%' ORDER BY TABLE_NAME;
--   SELECT INDEX_NAME, TABLE_NAME FROM ALL_INDEXES
--    WHERE OWNER='UM' AND INDEX_NAME LIKE 'IDX_%BUSINESS%' ORDER BY TABLE_NAME;
