-- =============================================================================
-- Travel agency: UM_APPLICATIONS + UM_MENUS
--
-- Your catalog today (example): ID 9 Home, 10 Setup, 11 Management — no Travel app yet.
-- This script creates application NAME = 'Travel' and attaches all /travel/* menu routes.
--
-- Sequences (JPA): UM.S_UM_APPLICATIONS, UM.S_UM_MENUS
-- Run once on Oracle as schema UM (or adjust UM. prefix).
--
-- After run:
--   SELECT ID, NAME, ROUTE FROM UM.UM_APPLICATIONS ORDER BY ID;
--   SELECT ID, APPLICATION_ID, NAME, ROUTE FROM UM.UM_MENUS WHERE ROUTE LIKE '/travel/%' ORDER BY ROUTE;
-- Grant VIEW/ADD/EDIT/DELETE on these routes in UM role menu matrix for travel roles.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1) Application row (sidebar top-level group)
-- -----------------------------------------------------------------------------
INSERT INTO UM.UM_APPLICATIONS (ID, NAME, DESCRIPTION, ICON, ROUTE, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_APPLICATIONS.NEXTVAL,
       'Travel',
       'Travel agency — clients, bookings, packages, visa, finance',
       'flight',
       '/travel',
       1,
       NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM UM.UM_APPLICATIONS
  WHERE UPPER(TRIM(NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
);

-- -----------------------------------------------------------------------------
-- 2) Menus — all rows use APPLICATION_ID of the Travel application
--    (Use the main app dashboard at /dashboard — no /travel/dashboard menu.)
-- -----------------------------------------------------------------------------
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Clients', '/travel/clients', 'people', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1 AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/clients');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Bookings', '/travel/bookings', 'event', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1 AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/bookings');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Tour packages', '/travel/packages', 'card_travel', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1 AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/packages');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Visa management', '/travel/visas', 'badge', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1 AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/visas');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Documents', '/travel/documents', 'folder', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1 AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/documents');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Suppliers', '/travel/suppliers', 'store', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1 AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/suppliers');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Payments', '/travel/finance', 'payments', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1 AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/finance');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Commissions', '/travel/commissions', 'percent', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1 AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/commissions');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Follow-ups', '/travel/follow-ups', 'notifications', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1 AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/follow-ups');

COMMIT;

-- If INSERTs for menus did nothing, the Travel application row is missing — check:
--   SELECT * FROM UM.UM_APPLICATIONS WHERE UPPER(NAME) LIKE '%TRAVEL%';
