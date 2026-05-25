-- Remove Travel built-in reports menu; use query/report builder instead.

DELETE FROM UM.UM_ROLE_MENU_PERM
WHERE MENU_ID IN (SELECT ID FROM UM.UM_MENUS WHERE ROUTE = '/travel/reports');

DELETE FROM UM.UM_MENUS WHERE ROUTE = '/travel/reports';

COMMIT;
