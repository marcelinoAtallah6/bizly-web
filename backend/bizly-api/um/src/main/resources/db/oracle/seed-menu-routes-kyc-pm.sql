-- Optional seed: KYC Customers + PM Products screens for UM_MENUS.route (adjust APPLICATION names / IDs to match your catalog).
-- After running, POST /um/menu/permission-metadata should list /kyc/customers and /pm/products.

-- KYC → Customers list (Angular: /kyc/customers)
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Customers', '/kyc/customers', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE UPPER(a.NAME) LIKE '%KYC%'
  AND ROWNUM = 1
  AND NOT EXISTS (
    SELECT 1 FROM UM.UM_MENUS m WHERE m.APPLICATION_ID = a.ID AND m.ROUTE = '/kyc/customers'
  );

-- PM → Products list (Angular: /pm/products)
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Products', '/pm/products', NULL, 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE (UPPER(a.NAME) LIKE '%PRODUCT%' OR UPPER(a.NAME) LIKE '%PM%' OR UPPER(a.NAME) LIKE '%STORE%')
  AND ROWNUM = 1
  AND NOT EXISTS (
    SELECT 1 FROM UM.UM_MENUS m WHERE m.APPLICATION_ID = a.ID AND m.ROUTE = '/pm/products'
  );

COMMIT;
