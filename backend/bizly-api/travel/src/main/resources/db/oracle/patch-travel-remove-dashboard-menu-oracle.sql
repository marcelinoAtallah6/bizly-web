-- Remove legacy Travel-specific dashboard menu; use main /dashboard instead.
-- Also delete role-menu grants for that menu id before removing the menu row.

DELETE FROM UM.UM_ROLE_MENU_PERM
WHERE MENU_ID IN (SELECT ID FROM UM.UM_MENUS WHERE ROUTE = '/travel/dashboard');

DELETE FROM UM.UM_MENUS WHERE ROUTE = '/travel/dashboard';

COMMIT;
