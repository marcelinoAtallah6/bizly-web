-- =============================================================================
-- UM_MENUS rows for Settings / dashboard tooling (Angular lazy routes).
-- Schema: UM. Run after UM_APPLICATIONS / UM_MENUS exist.
--
-- Angular routes (see frontend/src/app/app-routing.module.ts):
--   /dashboard              — Classic home + runtime widgets (dashboard chosen in header by id)
--   /qbe                    — Query Builder (saved queries)
--   /dash                   — Dashboard Builder (layouts + widgets)
--
-- Users open assigned dashboards from the header dropdown on /dashboard (no slug URL).
--
-- Parent APPLICATION_ID is resolved by name pattern (same style as seed-um-menus-all-routes.sql).
-- Tune WHERE clauses if your UM.UM_APPLICATIONS.NAME values differ.
-- =============================================================================

-- Query Builder — /qbe
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Query Builder', '/qbe', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/qbe');

-- Dashboard Builder — /dash
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Dashboard Builder', '/dash', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/dash');

-- Report Builder — /reporting (Settings module). Users define reports here;
-- each ACTIVE row in UM.SETTINGS_REPORT also appears in the sidebar's
-- synthetic "Reports" group (see frontend MenuCatalogService) and is
-- runnable at /reporting/run/<id>. Removing this menu does not affect the
-- generated reports — only the builder admin screen.
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Report Builder', '/reporting', 'report-analytics', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/reporting');

-- Rename existing Reporting menu in place so upgrades pick up the new label
-- without needing a manual data fix.
UPDATE UM.UM_MENUS
   SET NAME = 'Report Builder', ICON = COALESCE(ICON, 'report-analytics')
 WHERE ROUTE = '/reporting' AND NAME = 'Reporting';

COMMIT;

-- Verify:
-- SELECT ROUTE, NAME FROM UM.UM_MENUS WHERE ROUTE IN ('/dashboard','/qbe','/dash','/reporting') ORDER BY ROUTE;
