-- Application Builder — portal root admin catalog for UM_APPLICATIONS + UM_MENUS.
-- Run after patch-um-menu-sort-order-oracle.sql

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES, SORT_ORDER)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Application builder', '/um/application-builder', 'layout-grid-add', 1, NULL,
       COALESCE((SELECT MAX(m.SORT_ORDER) + 10 FROM UM.UM_MENUS m WHERE m.APPLICATION_ID = a.ID), 10)
FROM UM.UM_APPLICATIONS a
WHERE (UPPER(a.NAME) LIKE '%UM%' OR UPPER(a.NAME) LIKE '%USER MANAGEMENT%' OR UPPER(a.NAME) = 'UM')
  AND a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/um/application-builder');

COMMIT;
