-- =============================================================================
-- Seed UM.UM_MENUS with Angular / permission-matrix routes used in Bizly Web.
-- Table: UM.UM_MENUS (columns match JPA UmMenu + existing seeds)
-- ID:     UM.S_UM_MENUS.NEXTVAL (adjust if your sequence name differs)
--
-- Before running:  SELECT ID, NAME FROM UM.UM_APPLICATIONS ORDER BY NAME;
-- The INSERT ... SELECT blocks resolve APPLICATION_ID by NAME pattern.
-- If a row is skipped (no matching app), create the application first or add
-- a manual INSERT at the bottom with a literal APPLICATION_ID.
--
-- Routes required by @RequireMenuPermission (must match UM_MENUS.ROUTE):
--   /um/user  /um/role  /um/audit  /kyc/customers  /pm/products
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Dashboard (home) — attach to an app whose name suggests main/home/dashboard
-- -----------------------------------------------------------------------------
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Dashboard', '/dashboard', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND (UPPER(a.NAME) LIKE '%DASH%' OR UPPER(a.NAME) LIKE '%HOME%' OR UPPER(a.NAME) LIKE '%MAIN%')
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/dashboard');

-- Fallback: if no “dashboard/home” app, use first active application (review & adjust)
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Dashboard', '/dashboard', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/dashboard');

-- -----------------------------------------------------------------------------
-- User Management (UM) — application name pattern (tune to your catalog)
-- -----------------------------------------------------------------------------
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Users', '/um/user', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE (UPPER(a.NAME) LIKE '%UM%' OR UPPER(a.NAME) LIKE '%USER MANAGEMENT%' OR UPPER(a.NAME) = 'UM')
  AND a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/um/user');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Roles', '/um/role', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE (UPPER(a.NAME) LIKE '%UM%' OR UPPER(a.NAME) LIKE '%USER MANAGEMENT%' OR UPPER(a.NAME) = 'UM')
  AND a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/um/role');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Audit log', '/um/audit', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE (UPPER(a.NAME) LIKE '%UM%' OR UPPER(a.NAME) LIKE '%USER MANAGEMENT%' OR UPPER(a.NAME) = 'UM')
  AND a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/um/audit');

-- -----------------------------------------------------------------------------
-- KYC
-- -----------------------------------------------------------------------------
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Customers', '/kyc/customers', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE UPPER(a.NAME) LIKE '%KYC%'
  AND a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/kyc/customers');

-- -----------------------------------------------------------------------------
-- PM / Store / Products
-- -----------------------------------------------------------------------------
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Products', '/pm/products', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE (UPPER(a.NAME) LIKE '%PRODUCT%' OR UPPER(a.NAME) LIKE '%PM%' OR UPPER(a.NAME) LIKE '%STORE%')
  AND a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/pm/products');

-- -----------------------------------------------------------------------------
-- Optional: other lazy-loaded app roots (uncomment if those UM_APPLICATIONS exist)
-- -----------------------------------------------------------------------------
/*
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Appointments', '/apt', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a WHERE UPPER(a.NAME) LIKE '%APT%' OR UPPER(a.NAME) LIKE '%APPOINT%' AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/apt');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Payments', '/pay', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a WHERE UPPER(a.NAME) LIKE '%PAY%' AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/pay');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Configuration', '/conf', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a WHERE UPPER(a.NAME) LIKE '%CONF%' AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/conf');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Query builder', '/qbe', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a WHERE UPPER(a.NAME) LIKE '%QUERY%' OR UPPER(a.NAME) LIKE '%SETUP%' AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/qbe');

-- Reports menu was retired — the sidebar now renders one entry per active
-- row in UM.SETTINGS_REPORT (Report Builder), under a synthetic "Reports"
-- group injected by MenuCatalogService. The legacy /rpt placeholder is gone.

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'API builder', '/api', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a WHERE UPPER(a.NAME) LIKE '%API%' AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/api');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Dashboard builder', '/dash', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a WHERE UPPER(a.NAME) LIKE '%DASH%' AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/dash');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'UM builder', '/um-builder', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a WHERE UPPER(a.NAME) LIKE '%BUILD%' AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/um-builder');
*/

COMMIT;

-- Verify:
-- SELECT ROUTE, NAME, APPLICATION_ID FROM UM.UM_MENUS ORDER BY ROUTE;
