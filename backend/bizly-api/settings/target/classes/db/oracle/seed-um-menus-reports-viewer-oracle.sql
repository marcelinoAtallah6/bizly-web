-- =============================================================================
-- End-user Reports viewer (read-only) at Angular route /reports.
-- Report Builder MUST stay on route /reporting (different menu row).
-- =============================================================================

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Reports', '/reports', 'report', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND UPPER(TRIM(a.NAME)) LIKE '%SETUP%'
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/reports');

UPDATE UM.UM_MENUS
   SET ROUTE = '/reports'
 WHERE ROUTE = '/reporting'
   AND UPPER(TRIM(NAME)) = 'REPORTS'
   AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/reports');

COMMIT;
