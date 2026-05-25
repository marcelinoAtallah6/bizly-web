-- =============================================================================
-- UM_MENUS rows for Settings / dashboard tooling (Angular lazy routes).
-- Schema: UM. Run after UM_APPLICATIONS / UM_MENUS exist.
--
-- Angular routes (see frontend/src/app/app-routing.module.ts):
--   /dashboard              — Classic home + runtime widgets
--   /qbe                    — Query Builder
--   /dash                   — Dashboard Builder
--   /reporting              — Report Builder
--   /reports                — Reports viewer
-- =============================================================================

-- Query Builder — /qbe
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Query Builder', '/qbe', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND UPPER(TRIM(a.NAME)) LIKE '%SETUP%'
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/qbe');

-- Dashboard Builder — /dash
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Dashboard Builder', '/dash', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND UPPER(TRIM(a.NAME)) LIKE '%SETUP%'
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/dash');

-- Report Builder — /reporting
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Report Builder', '/reporting', 'report-analytics', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND UPPER(TRIM(a.NAME)) LIKE '%SETUP%'
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/reporting');

UPDATE UM.UM_MENUS
   SET NAME = 'Report Builder', ICON = COALESCE(ICON, 'report-analytics')
 WHERE ROUTE = '/reporting' AND NAME = 'Reporting';

-- End-user Reports viewer — /reports
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Reports', '/reports', 'report', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND UPPER(TRIM(a.NAME)) LIKE '%SETUP%'
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/reports');

COMMIT;
