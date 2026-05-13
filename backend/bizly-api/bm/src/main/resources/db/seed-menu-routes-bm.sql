-- =============================================================================
-- Seed UM.UM_MENUS routes for BM (Booking Management) API permission checks.
--
-- Backend @RequireMenuPermission(menuRoute = ...) must match UM_MENUS.ROUTE:
--   /bm/services      → ServiceItemController (service catalog / CRUD)
--   /bm/appointments  → AppointmentController (calendar / bookings)
--
-- Angular lazy routes should use the SAME paths (e.g. loadChildren under /bm/services).
-- After seeding: grant matrix permissions per role (UM UI or UM_ROLE_MENU_PERM).
--
-- Pick APPLICATION_ID: tune WHERE clause to your UM.UM_APPLICATIONS row (BM / Booking / Store).
-- =============================================================================

-- Services management screen
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Services', '/bm/services', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND (
    UPPER(a.NAME) LIKE '%BM%'
    OR UPPER(a.NAME) LIKE '%BOOK%'
    OR UPPER(a.NAME) LIKE '%APPOINT%'
    OR UPPER(a.NAME) LIKE '%STORE%'
    OR UPPER(a.NAME) LIKE '%PM%'
  )
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/bm/services');

-- Appointments / calendar screen
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Appointments', '/bm/appointments', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND (
    UPPER(a.NAME) LIKE '%BM%'
    OR UPPER(a.NAME) LIKE '%BOOK%'
    OR UPPER(a.NAME) LIKE '%APPOINT%'
    OR UPPER(a.NAME) LIKE '%STORE%'
    OR UPPER(a.NAME) LIKE '%PM%'
  )
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/bm/appointments');

COMMIT;

-- Verify:
-- SELECT ID, NAME, ROUTE, APPLICATION_ID FROM UM.UM_MENUS WHERE ROUTE LIKE '/bm/%' ORDER BY ROUTE;
