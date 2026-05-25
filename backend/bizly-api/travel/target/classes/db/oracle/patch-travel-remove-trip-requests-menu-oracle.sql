-- Trip requests merged into Bookings screen (ENQUIRY / QUOTED statuses)
DELETE FROM UM.UM_ROLE_MENU_PERM p
WHERE p.MENU_ID IN (SELECT m.ID FROM UM.UM_MENUS m WHERE m.ROUTE = '/travel/trip-requests');

DELETE FROM UM.UM_MENUS WHERE ROUTE = '/travel/trip-requests';

COMMIT;
