-- =============================================================================
-- Bizly — performance indexes + light tuning (Oracle)
--
-- Adds B-tree and function-based indexes on the columns each backend module
-- filters / joins / sorts on most often. Designed to be:
--
--   * Idempotent — every CREATE INDEX is wrapped in a helper that swallows
--     ORA-00955 (name in use), ORA-01408 (column list already indexed),
--     ORA-00942 (table missing in this env) and ORA-00904 (column missing).
--     So you can re-run the script after every release without errors.
--
--   * Schema-aware — every object is qualified UM.* explicitly; run as a user
--     with CREATE ANY INDEX privilege OR as the UM owner.
--
--   * Non-destructive — does not drop anything, does not rebuild indexes,
--     does not touch existing PK / UK constraints. Only adds.
--
-- After running this script, run the DBMS_STATS block at the bottom (or your
-- regular nightly stats job) so the optimizer actually picks the new indexes.
--
-- Tested against the schema produced by:
--   bizly-api/settings/01-settings-ddl.sql + patches
--   bizly-api/um security-and-audit-columns.sql
--   bizly-api/bm patch-bm-service-appointment-oracle.sql + sale-line patches
--   bizly-api/pm patch-pm-stock-and-sale-customer-display-oracle.sql
--   bizly-processes/notification 01-notification-ddl.sql
-- =============================================================================

SET DEFINE OFF;
SET SERVEROUTPUT ON SIZE UNLIMITED;
WHENEVER SQLERROR CONTINUE;

DECLARE
  -- Tolerated errors per statement (object already exists / table or column
  -- missing in this environment / column list already indexed).
  TYPE t_ignored IS TABLE OF NUMBER;
  v_ignored t_ignored := t_ignored(-955, -1408, -942, -904);

  PROCEDURE try_ddl(p_label VARCHAR2, p_sql VARCHAR2) IS
    v_skip BOOLEAN := FALSE;
  BEGIN
    EXECUTE IMMEDIATE p_sql;
    DBMS_OUTPUT.PUT_LINE('[CREATED] ' || p_label);
  EXCEPTION
    WHEN OTHERS THEN
      FOR i IN 1 .. v_ignored.COUNT LOOP
        IF SQLCODE = v_ignored(i) THEN
          v_skip := TRUE;
          EXIT;
        END IF;
      END LOOP;
      IF v_skip THEN
        DBMS_OUTPUT.PUT_LINE('[SKIP   ] ' || p_label || ' — ' || SQLERRM);
      ELSE
        DBMS_OUTPUT.PUT_LINE('[ERROR  ] ' || p_label || ' — ' || SQLERRM);
      END IF;
  END;

BEGIN
  ---------------------------------------------------------------------------
  -- UM_USER — auth, listing, lockout cron, notification cron
  ---------------------------------------------------------------------------
  -- username & email already have UNIQUE indexes; add function-based ones for
  -- case-insensitive lookups that the repository does via UPPER/LOWER.
  try_ddl('IX_UM_USER_UPPER_USERNAME',
          'CREATE INDEX UM.IX_UM_USER_UPPER_USERNAME ON UM.UM_USER (UPPER(TRIM(USERNAME)))');
  try_ddl('IX_UM_USER_UPPER_EMAIL',
          'CREATE INDEX UM.IX_UM_USER_UPPER_EMAIL ON UM.UM_USER (UPPER(TRIM(EMAIL)))');

  try_ddl('IX_UM_USER_STATUS',
          'CREATE INDEX UM.IX_UM_USER_STATUS ON UM.UM_USER (STATUS)');
  try_ddl('IX_UM_USER_CREATED_AT',
          'CREATE INDEX UM.IX_UM_USER_CREATED_AT ON UM.UM_USER (CREATED_AT)');

  -- Lockout / failed-login cron — find locked or failing users fast.
  try_ddl('IX_UM_USER_LOCKED',
          'CREATE INDEX UM.IX_UM_USER_LOCKED ON UM.UM_USER (ACCOUNT_LOCKED)');
  try_ddl('IX_UM_USER_FAIL_ATT',
          'CREATE INDEX UM.IX_UM_USER_FAIL_ATT ON UM.UM_USER (FAILED_LOGIN_ATTEMPTS)');

  -- Welcome-email batch picker: WHERE NOTIF_WELCOME_FLAG = 0 AND CREATED_AT > ?
  try_ddl('IX_UM_USER_WELCOME_PEND',
          'CREATE INDEX UM.IX_UM_USER_WELCOME_PEND ON UM.UM_USER (NOTIF_WELCOME_FLAG, CREATED_AT)');

  ---------------------------------------------------------------------------
  -- UM_ROLE — name lookups + role_type joins (now used by dashboard grants)
  ---------------------------------------------------------------------------
  try_ddl('IX_UM_ROLE_UPPER_NAME',
          'CREATE INDEX UM.IX_UM_ROLE_UPPER_NAME ON UM.UM_ROLE (UPPER(TRIM(NAME)))');
  try_ddl('IX_UM_ROLE_TYPE',
          'CREATE INDEX UM.IX_UM_ROLE_TYPE ON UM.UM_ROLE (ROLE_TYPE)');

  ---------------------------------------------------------------------------
  -- UM_USER_ROLE — PK already (user_id, role_id). Add reverse for "users by role".
  ---------------------------------------------------------------------------
  try_ddl('IX_UM_USER_ROLE_REV',
          'CREATE INDEX UM.IX_UM_USER_ROLE_REV ON UM.UM_USER_ROLE (ROLE_ID, USER_ID)');

  ---------------------------------------------------------------------------
  -- UM_USER_SESSION — login flow + cleanup
  ---------------------------------------------------------------------------
  try_ddl('IX_UM_SESS_USER',
          'CREATE INDEX UM.IX_UM_SESS_USER ON UM.UM_USER_SESSION (USER_ID)');
  try_ddl('IX_UM_SESS_SESSION_ID',
          'CREATE INDEX UM.IX_UM_SESS_SESSION_ID ON UM.UM_USER_SESSION (SESSION_ID)');
  try_ddl('IX_UM_SESS_EXPIRES',
          'CREATE INDEX UM.IX_UM_SESS_EXPIRES ON UM.UM_USER_SESSION (EXPIRES_AT)');
  -- Active-sessions-by-user lookup ("does user still have a live session?").
  try_ddl('IX_UM_SESS_USER_ACTIVE',
          'CREATE INDEX UM.IX_UM_SESS_USER_ACTIVE ON UM.UM_USER_SESSION (USER_ID, ACTIVE, EXPIRES_AT)');
  -- Audit dashboard "Sessions by active role".
  try_ddl('IX_UM_SESS_ROLE',
          'CREATE INDEX UM.IX_UM_SESS_ROLE ON UM.UM_USER_SESSION (ACTIVE_ROLE_NAME)');

  ---------------------------------------------------------------------------
  -- UM_AUDIT_LOG — timeline widgets + per-user/per-endpoint analytics
  ---------------------------------------------------------------------------
  try_ddl('IX_UM_AUDIT_CREATED',
          'CREATE INDEX UM.IX_UM_AUDIT_CREATED ON UM.UM_AUDIT_LOG (CREATED_AT)');
  try_ddl('IX_UM_AUDIT_USER_DAY',
          'CREATE INDEX UM.IX_UM_AUDIT_USER_DAY ON UM.UM_AUDIT_LOG (USERNAME, CREATED_AT)');
  try_ddl('IX_UM_AUDIT_ACTION_DAY',
          'CREATE INDEX UM.IX_UM_AUDIT_ACTION_DAY ON UM.UM_AUDIT_LOG (ACTION_CODE, CREATED_AT)');
  try_ddl('IX_UM_AUDIT_PATH',
          'CREATE INDEX UM.IX_UM_AUDIT_PATH ON UM.UM_AUDIT_LOG (REQUEST_PATH)');
  try_ddl('IX_UM_AUDIT_RES_TYPE_ID',
          'CREATE INDEX UM.IX_UM_AUDIT_RES_TYPE_ID ON UM.UM_AUDIT_LOG (RESOURCE_TYPE, RESOURCE_ID)');
  try_ddl('IX_UM_AUDIT_SESSION',
          'CREATE INDEX UM.IX_UM_AUDIT_SESSION ON UM.UM_AUDIT_LOG (SESSION_ID)');

  ---------------------------------------------------------------------------
  -- UM_APPLICATIONS / UM_MENUS — sidebar build
  ---------------------------------------------------------------------------
  try_ddl('IX_UM_APP_ACTIVE',
          'CREATE INDEX UM.IX_UM_APP_ACTIVE ON UM.UM_APPLICATIONS (IS_ACTIVE)');
  try_ddl('IX_UM_MENUS_APP',
          'CREATE INDEX UM.IX_UM_MENUS_APP ON UM.UM_MENUS (APPLICATION_ID)');
  try_ddl('IX_UM_MENUS_PARENT',
          'CREATE INDEX UM.IX_UM_MENUS_PARENT ON UM.UM_MENUS (PARENT_ID)');
  try_ddl('IX_UM_MENUS_ACTIVE',
          'CREATE INDEX UM.IX_UM_MENUS_ACTIVE ON UM.UM_MENUS (IS_ACTIVE)');
  try_ddl('IX_UM_MENUS_ROUTE',
          'CREATE INDEX UM.IX_UM_MENUS_ROUTE ON UM.UM_MENUS (ROUTE)');

  ---------------------------------------------------------------------------
  -- UM_ROLE_MENU_PERM — PK already (role_id, menu_id). Add reverse for menu→roles.
  ---------------------------------------------------------------------------
  try_ddl('IX_UM_RMP_MENU_ROLE',
          'CREATE INDEX UM.IX_UM_RMP_MENU_ROLE ON UM.UM_ROLE_MENU_PERM (MENU_ID, ROLE_ID)');

  ---------------------------------------------------------------------------
  -- KYC_CUSTOMER — list filters, search, CRM widgets, notification cron
  ---------------------------------------------------------------------------
  try_ddl('IX_KYC_CUST_STATUS',
          'CREATE INDEX UM.IX_KYC_CUST_STATUS ON UM.KYC_CUSTOMER (CUSTOMER_STATUS)');
  try_ddl('IX_KYC_CUST_CREATED',
          'CREATE INDEX UM.IX_KYC_CUST_CREATED ON UM.KYC_CUSTOMER (CREATED_AT)');
  -- Case-insensitive prefix search ("johns%" → uses index).
  try_ddl('IX_KYC_CUST_UFULLNAME',
          'CREATE INDEX UM.IX_KYC_CUST_UFULLNAME ON UM.KYC_CUSTOMER (UPPER(FULL_NAME))');
  try_ddl('IX_KYC_CUST_UEMAIL',
          'CREATE INDEX UM.IX_KYC_CUST_UEMAIL ON UM.KYC_CUSTOMER (UPPER(EMAIL))');
  try_ddl('IX_KYC_CUST_MOBILE',
          'CREATE INDEX UM.IX_KYC_CUST_MOBILE ON UM.KYC_CUSTOMER (MOBILE_NUMBER)');
  try_ddl('IX_KYC_CUST_COUNTRY',
          'CREATE INDEX UM.IX_KYC_CUST_COUNTRY ON UM.KYC_CUSTOMER (COUNTRY)');
  try_ddl('IX_KYC_CUST_WELCOME_PEND',
          'CREATE INDEX UM.IX_KYC_CUST_WELCOME_PEND ON UM.KYC_CUSTOMER (NOTIF_WELCOME_FLAG, CREATED_AT)');

  ---------------------------------------------------------------------------
  -- PM_PRODUCT — catalog list + stock widgets
  ---------------------------------------------------------------------------
  try_ddl('IX_PM_PROD_UNAME',
          'CREATE INDEX UM.IX_PM_PROD_UNAME ON UM.PM_PRODUCT (UPPER(NAME))');
  try_ddl('IX_PM_PROD_STOCK',
          'CREATE INDEX UM.IX_PM_PROD_STOCK ON UM.PM_PRODUCT (STOCK_QUANTITY)');
  try_ddl('IX_PM_PROD_CREATED',
          'CREATE INDEX UM.IX_PM_PROD_CREATED ON UM.PM_PRODUCT (CREATED_AT)');

  try_ddl('IX_PM_PROD_ITEMS_PROD',
          'CREATE INDEX UM.IX_PM_PROD_ITEMS_PROD ON UM.PM_PRODUCT_ITEMS (PRODUCT_ID)');

  ---------------------------------------------------------------------------
  -- PM_CUSTOMER_SALE — sales list, KPI widgets ("status + created_at" is hot)
  ---------------------------------------------------------------------------
  try_ddl('IX_PM_SALE_CUSTOMER',
          'CREATE INDEX UM.IX_PM_SALE_CUSTOMER ON UM.PM_CUSTOMER_SALE (CUSTOMER_ID)');
  try_ddl('IX_PM_SALE_STATUS',
          'CREATE INDEX UM.IX_PM_SALE_STATUS ON UM.PM_CUSTOMER_SALE (STATUS)');
  try_ddl('IX_PM_SALE_CREATED',
          'CREATE INDEX UM.IX_PM_SALE_CREATED ON UM.PM_CUSTOMER_SALE (CREATED_AT)');
  -- "Completed sales today / this month" widgets always pair these.
  try_ddl('IX_PM_SALE_STATUS_DAY',
          'CREATE INDEX UM.IX_PM_SALE_STATUS_DAY ON UM.PM_CUSTOMER_SALE (STATUS, CREATED_AT)');

  ---------------------------------------------------------------------------
  -- PM_CUSTOMER_SALE_LINE — top-products / fast-moving widgets do GROUP BY product_name + JOIN on sale_id.
  ---------------------------------------------------------------------------
  try_ddl('IX_PM_SALE_LINE_SALE',
          'CREATE INDEX UM.IX_PM_SALE_LINE_SALE ON UM.PM_CUSTOMER_SALE_LINE (SALE_ID)');
  try_ddl('IX_PM_SALE_LINE_PRODUCT',
          'CREATE INDEX UM.IX_PM_SALE_LINE_PRODUCT ON UM.PM_CUSTOMER_SALE_LINE (PRODUCT_ID)');
  try_ddl('IX_PM_SALE_LINE_SERVICE',
          'CREATE INDEX UM.IX_PM_SALE_LINE_SERVICE ON UM.PM_CUSTOMER_SALE_LINE (SERVICE_ID)');
  try_ddl('IX_PM_SALE_LINE_TYPE',
          'CREATE INDEX UM.IX_PM_SALE_LINE_TYPE ON UM.PM_CUSTOMER_SALE_LINE (LINE_TYPE)');

  ---------------------------------------------------------------------------
  -- BM_SERVICE_ITEM
  ---------------------------------------------------------------------------
  try_ddl('IX_BM_SVC_ACTIVE',
          'CREATE INDEX UM.IX_BM_SVC_ACTIVE ON UM.BM_SERVICE_ITEM (ACTIVE)');
  try_ddl('IX_BM_SVC_UNAME',
          'CREATE INDEX UM.IX_BM_SVC_UNAME ON UM.BM_SERVICE_ITEM (UPPER(NAME))');
  try_ddl('IX_BM_SVC_CREATED',
          'CREATE INDEX UM.IX_BM_SVC_CREATED ON UM.BM_SERVICE_ITEM (CREATED_AT)');

  ---------------------------------------------------------------------------
  -- BM_APPOINTMENT — calendar range scans
  --   patch already shipped: IDX_BM_APPT_CUSTOMER_TIME, IDX_BM_APPT_START.
  --   Add: service_id, status, status+start_time (calendar status filter), end_time.
  ---------------------------------------------------------------------------
  try_ddl('IX_BM_APPT_SERVICE',
          'CREATE INDEX UM.IX_BM_APPT_SERVICE ON UM.BM_APPOINTMENT (SERVICE_ID)');
  try_ddl('IX_BM_APPT_STATUS',
          'CREATE INDEX UM.IX_BM_APPT_STATUS ON UM.BM_APPOINTMENT (STATUS)');
  try_ddl('IX_BM_APPT_STATUS_START',
          'CREATE INDEX UM.IX_BM_APPT_STATUS_START ON UM.BM_APPOINTMENT (STATUS, START_TIME)');
  try_ddl('IX_BM_APPT_END',
          'CREATE INDEX UM.IX_BM_APPT_END ON UM.BM_APPOINTMENT (END_TIME)');

  ---------------------------------------------------------------------------
  -- Settings
  --   SETTINGS_DASHBOARD: UX_SETTINGS_DASH_SLUG already created in DDL.
  --   SETTINGS_WIDGET   : IX_SETTINGS_WIDGET_DASH already created in DDL.
  --   SETTINGS_DASH_ROLE_GRANT: PK (dashboard_id, role_type) covers FK lookups by dashboard_id;
  --                              add reverse on role_type for findByIdRoleType.
  --   SETTINGS_DASH_USER_GRANT: PK (dashboard_id, username). Reverse on UPPER(username).
  --   SETTINGS_NAV_PREF       : PK (username, dashboard_id) already covers
  --                              findByIdUsernameIgnoreCase by prefix, but make it case-insensitive.
  --   SETTINGS_USER_PREF      : last_dashboard_id FK index.
  --   SETTINGS_QUERY_DEF      : UX on UPPER(TRIM(NAME)) already in DDL.
  ---------------------------------------------------------------------------
  try_ddl('IX_SETT_WIDGET_QUERY',
          'CREATE INDEX UM.IX_SETT_WIDGET_QUERY ON UM.SETTINGS_WIDGET (QUERY_DEF_ID)');
  try_ddl('IX_SETT_WIDGET_TYPE',
          'CREATE INDEX UM.IX_SETT_WIDGET_TYPE ON UM.SETTINGS_WIDGET (WIDGET_TYPE)');
  try_ddl('IX_SETT_DRG_ROLE_TYPE',
          'CREATE INDEX UM.IX_SETT_DRG_ROLE_TYPE ON UM.SETTINGS_DASH_ROLE_GRANT (ROLE_TYPE)');
  try_ddl('IX_SETT_DUG_UUSER',
          'CREATE INDEX UM.IX_SETT_DUG_UUSER ON UM.SETTINGS_DASH_USER_GRANT (UPPER(USERNAME))');
  try_ddl('IX_SETT_NAV_UUSER',
          'CREATE INDEX UM.IX_SETT_NAV_UUSER ON UM.SETTINGS_NAV_PREF (UPPER(USERNAME))');
  try_ddl('IX_SETT_USER_PREF_LAST',
          'CREATE INDEX UM.IX_SETT_USER_PREF_LAST ON UM.SETTINGS_USER_PREF (LAST_DASHBOARD_ID)');

  ---------------------------------------------------------------------------
  -- Broadcast + notification
  ---------------------------------------------------------------------------
  try_ddl('IX_BC_MSG_STATUS',
          'CREATE INDEX UM.IX_BC_MSG_STATUS ON UM.NOTIF_BROADCAST_MESSAGE (STATUS)');
  try_ddl('IX_BC_MSG_CREATED',
          'CREATE INDEX UM.IX_BC_MSG_CREATED ON UM.NOTIF_BROADCAST_MESSAGE (CREATED_AT)');
  try_ddl('IX_BC_MSG_STATUS_SENT',
          'CREATE INDEX UM.IX_BC_MSG_STATUS_SENT ON UM.NOTIF_BROADCAST_MESSAGE (STATUS, SENT_AT)');
  try_ddl('IX_BC_MSG_TARGET_ROLE',
          'CREATE INDEX UM.IX_BC_MSG_TARGET_ROLE ON UM.NOTIF_BROADCAST_MESSAGE (TARGET_ROLE_ID)');

  -- notif_dispatch_log has IDX_NOTIF_DISPATCH_USER_DAY (USER_ID, PROCESS_TYPE, CREATED_AT) shipped.
  try_ddl('IX_NOTIF_DISP_STATUS_DAY',
          'CREATE INDEX UM.IX_NOTIF_DISP_STATUS_DAY ON UM.NOTIF_DISPATCH_LOG (STATUS, CREATED_AT)');
  try_ddl('IX_NOTIF_DISP_TMPL',
          'CREATE INDEX UM.IX_NOTIF_DISP_TMPL ON UM.NOTIF_DISPATCH_LOG (TEMPLATE_KEY)');
  try_ddl('IX_NOTIF_DISP_RECIP',
          'CREATE INDEX UM.IX_NOTIF_DISP_RECIP ON UM.NOTIF_DISPATCH_LOG (UPPER(RECIPIENT_EMAIL))');

  COMMIT;
  DBMS_OUTPUT.PUT_LINE('=== index creation pass completed ===');
END;
/

-- =============================================================================
-- Light tuning: refresh statistics so the optimizer actually uses the new
-- indexes. Safe to run as the UM owner. Takes a few seconds to a few minutes
-- depending on data volume.
-- =============================================================================
BEGIN
  DBMS_STATS.GATHER_SCHEMA_STATS(
    ownname          => 'UM',
    estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
    method_opt       => 'FOR ALL COLUMNS SIZE AUTO',
    cascade          => TRUE,           -- indexes too
    no_invalidate    => FALSE,          -- bust stale cursors so plans get re-chosen
    degree           => DBMS_STATS.DEFAULT_DEGREE
  );
  DBMS_OUTPUT.PUT_LINE('=== UM schema stats refreshed ===');
END;
/

-- =============================================================================
-- Optional: targeted hints / session-level toggles. Uncomment as needed.
-- =============================================================================

-- 1) If you see "index ignored" patterns where you know an index would win,
--    nudge the optimizer slightly (DBA decides — affects ALL queries):
-- ALTER SYSTEM SET OPTIMIZER_INDEX_COST_ADJ   = 25  SCOPE = BOTH;
-- ALTER SYSTEM SET OPTIMIZER_INDEX_CACHING    = 80  SCOPE = BOTH;

-- 2) Lots of similar SQL with literal values (legacy callers, ad-hoc reports)?
--    Force binds so the cursor cache hits more often:
-- ALTER SYSTEM SET CURSOR_SHARING = 'FORCE' SCOPE = BOTH;

-- 3) Rebuild bloated indexes after large purges. Not needed at first install;
--    run when a table has churned a lot (audit log archival, sessions cleanup, …):
-- ALTER INDEX UM.IX_UM_AUDIT_CREATED REBUILD ONLINE;
-- ALTER INDEX UM.IX_UM_SESS_EXPIRES  REBUILD ONLINE;

-- 4) Sanity-check what got created (re-run to inspect):
-- SELECT INDEX_NAME, TABLE_NAME, COLUMN_NAME, COLUMN_POSITION
--   FROM ALL_IND_COLUMNS
--  WHERE INDEX_OWNER = 'UM' AND INDEX_NAME LIKE 'IX\_%' ESCAPE '\\'
--  ORDER BY TABLE_NAME, INDEX_NAME, COLUMN_POSITION;
