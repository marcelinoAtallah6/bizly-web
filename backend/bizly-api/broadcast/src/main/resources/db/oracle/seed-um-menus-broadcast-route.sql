-- =============================================================================
-- Seed UM.UM_MENUS route for Broadcast UI (must match Angular + broadcast API).
--
-- Backend @RequireMenuPermission(menuRoute = "/broadcast") on
-- com.broadcast.api.controller.broadcast.BroadcastMessageController
-- Angular lazy route: /broadcast (see app-routing → broadcast).
--
-- After seeding: grant VIEW / ADD (and edit/delete if desired) per role in UM UI
-- or UM_ROLE_MENU_PERM so the JWT permMatrix includes this route.
-- =============================================================================

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Broadcast', '/broadcast', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE a.IS_ACTIVE = 1
  AND (
    UPPER(a.NAME) LIKE '%UM%'
    OR UPPER(a.NAME) LIKE '%USER%'
    OR UPPER(a.NAME) LIKE '%ADMIN%'
    OR UPPER(a.NAME) LIKE '%STORE%'
    OR UPPER(a.NAME) LIKE '%BIZ%'
  )
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/broadcast');

COMMIT;

-- Verify:
-- SELECT ID, NAME, ROUTE, APPLICATION_ID FROM UM.UM_MENUS WHERE ROUTE = '/broadcast';
