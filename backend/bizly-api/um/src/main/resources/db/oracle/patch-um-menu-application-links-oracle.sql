-- =============================================================================
-- Re-link UM_MENUS rows to the correct UM_APPLICATIONS group.
-- Fixes setup tools (Query/Dashboard/Report builder) showing under Management
-- because seeds used "AND ROWNUM = 1" on the first active application.
-- =============================================================================

-- Setup application (builder / configuration tools)
UPDATE UM.UM_MENUS m
   SET m.APPLICATION_ID = (
         SELECT a.ID FROM UM.UM_APPLICATIONS a
          WHERE a.IS_ACTIVE = 1
            AND UPPER(TRIM(a.NAME)) LIKE '%SETUP%'
          ORDER BY a.ID
          FETCH FIRST 1 ROW ONLY
       )
 WHERE m.ROUTE IN ('/qbe', '/dash', '/reporting', '/reports', '/api', '/um-builder');

-- Home / dashboard entry
UPDATE UM.UM_MENUS m
   SET m.APPLICATION_ID = (
         SELECT a.ID FROM UM.UM_APPLICATIONS a
          WHERE a.IS_ACTIVE = 1
            AND (UPPER(TRIM(a.NAME)) LIKE '%HOME%'
              OR UPPER(TRIM(a.NAME)) LIKE '%DASH%'
              OR UPPER(TRIM(a.NAME)) LIKE '%MAIN%')
          ORDER BY a.ID
          FETCH FIRST 1 ROW ONLY
       )
 WHERE m.ROUTE = '/dashboard';

-- User Management (UM module screens)
UPDATE UM.UM_MENUS m
   SET m.APPLICATION_ID = (
         SELECT a.ID FROM UM.UM_APPLICATIONS a
          WHERE a.IS_ACTIVE = 1
            AND (UPPER(TRIM(a.NAME)) LIKE '%UM%'
              OR UPPER(TRIM(a.NAME)) LIKE '%USER MANAGEMENT%'
              OR UPPER(TRIM(a.NAME)) = 'UM')
          ORDER BY a.ID
          FETCH FIRST 1 ROW ONLY
       )
 WHERE m.ROUTE LIKE '/um/%';

-- Travel agency
UPDATE UM.UM_MENUS m
   SET m.APPLICATION_ID = (
         SELECT a.ID FROM UM.UM_APPLICATIONS a
          WHERE a.IS_ACTIVE = 1
            AND UPPER(TRIM(a.NAME)) IN ('TRAVEL', 'TRAVEL AGENCY')
          ORDER BY a.ID
          FETCH FIRST 1 ROW ONLY
       )
 WHERE m.ROUTE LIKE '/travel/%';

-- KYC
UPDATE UM.UM_MENUS m
   SET m.APPLICATION_ID = (
         SELECT a.ID FROM UM.UM_APPLICATIONS a
          WHERE a.IS_ACTIVE = 1
            AND UPPER(TRIM(a.NAME)) LIKE '%KYC%'
          ORDER BY a.ID
          FETCH FIRST 1 ROW ONLY
       )
 WHERE m.ROUTE LIKE '/kyc/%';

-- Product management
UPDATE UM.UM_MENUS m
   SET m.APPLICATION_ID = (
         SELECT a.ID FROM UM.UM_APPLICATIONS a
          WHERE a.IS_ACTIVE = 1
            AND (UPPER(TRIM(a.NAME)) LIKE '%PRODUCT%'
              OR UPPER(TRIM(a.NAME)) LIKE '%PM%'
              OR UPPER(TRIM(a.NAME)) LIKE '%STORE%')
          ORDER BY a.ID
          FETCH FIRST 1 ROW ONLY
       )
 WHERE m.ROUTE LIKE '/pm/%';

-- Booking management
UPDATE UM.UM_MENUS m
   SET m.APPLICATION_ID = (
         SELECT a.ID FROM UM.UM_APPLICATIONS a
          WHERE a.IS_ACTIVE = 1
            AND (UPPER(TRIM(a.NAME)) LIKE '%BM%'
              OR UPPER(TRIM(a.NAME)) LIKE '%BOOK%'
              OR UPPER(TRIM(a.NAME)) LIKE '%APPOINT%')
          ORDER BY a.ID
          FETCH FIRST 1 ROW ONLY
       )
 WHERE m.ROUTE LIKE '/bm/%';

-- Broadcast
UPDATE UM.UM_MENUS m
   SET m.APPLICATION_ID = (
         SELECT a.ID FROM UM.UM_APPLICATIONS a
          WHERE a.IS_ACTIVE = 1
            AND UPPER(TRIM(a.NAME)) LIKE '%BROADCAST%'
          ORDER BY a.ID
          FETCH FIRST 1 ROW ONLY
       )
 WHERE m.ROUTE LIKE '/broadcast/%';

COMMIT;

-- Verify:
-- SELECT a.NAME, m.ROUTE, m.NAME AS MENU_NAME
--   FROM UM.UM_MENUS m
--   JOIN UM.UM_APPLICATIONS a ON a.ID = m.APPLICATION_ID
--  WHERE m.ROUTE IN ('/qbe','/dash','/reporting','/reports')
--  ORDER BY a.NAME, m.ROUTE;
