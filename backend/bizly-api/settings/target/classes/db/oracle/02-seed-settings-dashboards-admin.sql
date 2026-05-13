-- =============================================================================
-- Seed predefined SETTINGS_QUERY_DEF rows + SETTINGS_DASHBOARD + SETTINGS_WIDGET
-- + SETTINGS_DASH_ROLE_GRANT for role ADMIN (matches Spring ROLE_ADMIN after normalize).
--
-- Prerequisites:
--   UM.SETTINGS_* DDL applied (01-settings-ddl.sql)
--   Tables: UM.KYC_CUSTOMER, UM.PM_PRODUCT, UM.PM_CUSTOMER_SALE, UM.PM_CUSTOMER_SALE_LINE,
--           UM.UM_USER, UM.UM_ROLE, UM.UM_USER_ROLE, UM.UM_USER_SESSION, UM.UM_AUDIT_LOG
--   Optional columns (patches): UM.UM_USER.ACCOUNT_LOCKED, FAILED_LOGIN_ATTEMPTS,
--                               UM.PM_PRODUCT.STOCK_QUANTITY
--
-- Notes:
-- - Dynamic dashboard UI renders KPI as a single scalar (first column) and other types as tables.
--   Chart names (line/donut/heatmap) are represented as time-series or pivot tables until chart widgets exist.
-- - Payment "method" is not modeled on PM_CUSTOMER_SALE; STATUS is used as a breakdown instead.
-- - Product category is not on PM_PRODUCT; a placeholder distribution query is included.
--
-- Idempotent: removes prior seed rows named SEED_Q_% and dashboards with the slugs below.
-- =============================================================================

SET DEFINE OFF;

BEGIN
  FOR r IN (
    SELECT ID FROM UM.SETTINGS_DASHBOARD
    WHERE SLUG IN (
      'customer-overview',
      'payment-sales-overview',
      'stock-inventory',
      'user-management-activity',
      'audit-security-monitoring',
      'quick-actions-panel'
    )
  ) LOOP
    DELETE FROM UM.SETTINGS_WIDGET WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_DASH_ROLE_GRANT WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_NAV_PREF WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_DASHBOARD WHERE ID = r.ID;
  END LOOP;
  DELETE FROM UM.SETTINGS_QUERY_DEF WHERE NAME LIKE 'SEED_Q_%';
END;
/

DECLARE
  /* ----- Customer overview ----- */
  q_cust_total       NUMBER;
  q_cust_new_day     NUMBER;
  q_cust_new_week    NUMBER;
  q_cust_new_month   NUMBER;
  q_cust_status      NUMBER;
  q_cust_top10       NUMBER;
  q_cust_growth      NUMBER;

  /* ----- Payment & sales ----- */
  q_sale_day         NUMBER;
  q_sale_week        NUMBER;
  q_sale_month       NUMBER;
  q_sale_year        NUMBER;
  q_pay_breakdown    NUMBER;
  q_aov              NUMBER;
  q_top_products     NUMBER;
  q_sales_trend      NUMBER;
  q_sales_weekly     NUMBER;

  /* ----- Stock ----- */
  q_stock_units      NUMBER;
  q_low_stock        NUMBER;
  q_out_stock        NUMBER;
  q_fast_moving      NUMBER;
  q_slow_moving      NUMBER;
  q_stock_value      NUMBER;
  q_cat_dist         NUMBER;

  /* ----- UM activity ----- */
  q_users_tot        NUMBER;
  q_sess_today       NUMBER;
  q_locked           NUMBER;
  q_roles_dist       NUMBER;
  q_audit_day_cnt    NUMBER;
  q_audit_month_cnt  NUMBER;
  q_login_hours      NUMBER;

  /* ----- Audit & security ----- */
  q_audit_rows_today NUMBER;
  q_endpoints        NUMBER;
  q_failed_users     NUMBER;
  q_role_switch      NUMBER;
  q_audit_user_rank  NUMBER;
  q_audit_hourly     NUMBER;

  d_cust             NUMBER;
  d_pay              NUMBER;
  d_stock            NUMBER;
  d_um               NUMBER;
  d_audit            NUMBER;
  d_quick            NUMBER;

  v_admin_type       NUMBER;

  PROCEDURE ins_query(p_name VARCHAR2, p_desc VARCHAR2, p_sql CLOB, p_id OUT NUMBER) IS
  BEGIN
    INSERT INTO UM.SETTINGS_QUERY_DEF (NAME, DESCRIPTION, SQL_TEXT)
    VALUES (p_name, p_desc, p_sql)
    RETURNING ID INTO p_id;
  END;

  PROCEDURE ins_widget(
    p_dash NUMBER,
    p_type VARCHAR2,
    p_title VARCHAR2,
    p_qid NUMBER,
    p_cfg CLOB,
    p_x NUMBER,
    p_y NUMBER,
    p_w NUMBER,
    p_h NUMBER,
    p_ord NUMBER
  ) IS
  BEGIN
    INSERT INTO UM.SETTINGS_WIDGET (
      DASHBOARD_ID, WIDGET_TYPE, TITLE, QUERY_DEF_ID, CONFIG_JSON,
      GRID_X, GRID_Y, GRID_W, GRID_H, SORT_ORDER
    ) VALUES (
      p_dash, p_type, p_title, p_qid, p_cfg,
      p_x, p_y, p_w, p_h, p_ord
    );
  END;

BEGIN
  /* Resolve the role_type for ADMIN once; grants are keyed on role_type (stable across renames). */
  BEGIN
    SELECT ROLE_TYPE INTO v_admin_type
      FROM UM.UM_ROLE
     WHERE UPPER(TRIM(NAME)) = 'ADMIN'
       AND ROLE_TYPE IS NOT NULL
       AND ROWNUM = 1;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20001, 'Seed aborted: no UM_ROLE named ADMIN with a ROLE_TYPE configured.');
  END;

  /* ---------- Queries: Customer ---------- */
  ins_query(
    'SEED_Q_CUST_TOTAL',
    'Total customers',
    q'[SELECT COUNT(*) AS TOTAL FROM UM.KYC_CUSTOMER]',
    q_cust_total
  );
  ins_query(
    'SEED_Q_CUST_NEW_DAY',
    'New customers today',
    q'[SELECT COUNT(*) AS NEW_TODAY FROM UM.KYC_CUSTOMER WHERE TRUNC(CREATED_AT) = TRUNC(SYSDATE)]',
    q_cust_new_day
  );
  ins_query(
    'SEED_Q_CUST_NEW_WEEK',
    'New customers last 7 days',
    q'[SELECT COUNT(*) AS NEW_WEEK FROM UM.KYC_CUSTOMER WHERE CREATED_AT >= TRUNC(SYSDATE) - 6]',
    q_cust_new_week
  );
  ins_query(
    'SEED_Q_CUST_NEW_MONTH',
    'New customers this calendar month',
    q'[SELECT COUNT(*) AS NEW_MONTH FROM UM.KYC_CUSTOMER WHERE CREATED_AT >= TRUNC(SYSDATE, 'MM')]',
    q_cust_new_month
  );
  ins_query(
    'SEED_Q_CUST_STATUS',
    'Customers by status',
    q'[SELECT NVL(CUSTOMER_STATUS, 'UNKNOWN') AS STATUS_BUCKET, COUNT(*) AS CNT FROM UM.KYC_CUSTOMER GROUP BY NVL(CUSTOMER_STATUS, 'UNKNOWN')]',
    q_cust_status
  );
  ins_query(
    'SEED_Q_CUST_TOP10',
    'Top customers by completed purchase total',
    q'[SELECT * FROM (
         SELECT c.ID AS CUSTOMER_ID,
                COALESCE(c.FULL_NAME, c.EMAIL, c.MOBILE_NUMBER) AS CUSTOMER_NAME,
                SUM(s.TOTAL_AMOUNT) AS TOTAL_PURCHASES
           FROM UM.KYC_CUSTOMER c
           JOIN UM.PM_CUSTOMER_SALE s ON s.CUSTOMER_ID = c.ID
          WHERE s.STATUS = 'COMPLETED'
          GROUP BY c.ID, COALESCE(c.FULL_NAME, c.EMAIL, c.MOBILE_NUMBER)
          ORDER BY TOTAL_PURCHASES DESC
       ) WHERE ROWNUM <= 10]',
    q_cust_top10
  );
  ins_query(
    'SEED_Q_CUST_GROWTH',
    'New customers by month (last 12 months)',
    q'[SELECT TRUNC(CREATED_AT, 'MM') AS MONTH_START, COUNT(*) AS NEW_CUSTOMERS
         FROM UM.KYC_CUSTOMER
        WHERE CREATED_AT >= ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -11)
        GROUP BY TRUNC(CREATED_AT, 'MM')
        ORDER BY 1]',
    q_cust_growth
  );

  /* ---------- Queries: Sales ---------- */
  ins_query(
    'SEED_Q_SALE_DAY',
    'Completed sales amount today',
    q'[SELECT NVL(SUM(TOTAL_AMOUNT), 0) AS SALES_TODAY FROM UM.PM_CUSTOMER_SALE WHERE STATUS = 'COMPLETED' AND TRUNC(CREATED_AT) = TRUNC(SYSDATE)]',
    q_sale_day
  );
  ins_query(
    'SEED_Q_SALE_WEEK',
    'Completed sales last 7 days',
    q'[SELECT NVL(SUM(TOTAL_AMOUNT), 0) AS SALES_WEEK FROM UM.PM_CUSTOMER_SALE WHERE STATUS = 'COMPLETED' AND CREATED_AT >= TRUNC(SYSDATE) - 6]',
    q_sale_week
  );
  ins_query(
    'SEED_Q_SALE_MONTH',
    'Completed sales this month',
    q'[SELECT NVL(SUM(TOTAL_AMOUNT), 0) AS SALES_MONTH FROM UM.PM_CUSTOMER_SALE WHERE STATUS = 'COMPLETED' AND CREATED_AT >= TRUNC(SYSDATE, 'MM')]',
    q_sale_month
  );
  ins_query(
    'SEED_Q_SALE_YEAR',
    'Completed sales this year',
    q'[SELECT NVL(SUM(TOTAL_AMOUNT), 0) AS SALES_YEAR FROM UM.PM_CUSTOMER_SALE WHERE STATUS = 'COMPLETED' AND CREATED_AT >= TRUNC(SYSDATE, 'YYYY')]',
    q_sale_year
  );
  ins_query(
    'SEED_Q_PAY_BREAKDOWN',
    'Orders by status',
    q'[SELECT STATUS, COUNT(*) AS ORDER_COUNT, NVL(SUM(TOTAL_AMOUNT), 0) AS AMOUNT FROM UM.PM_CUSTOMER_SALE GROUP BY STATUS]',
    q_pay_breakdown
  );
  ins_query(
    'SEED_Q_AOV',
    'Average order value (completed)',
    q'[SELECT ROUND(AVG(TOTAL_AMOUNT), 2) AS AVG_ORDER_VALUE FROM UM.PM_CUSTOMER_SALE WHERE STATUS = 'COMPLETED']',
    q_aov
  );
  ins_query(
    'SEED_Q_TOP_PRODUCTS',
    'Top products by units sold',
    q'[SELECT * FROM (
         SELECT l.PRODUCT_NAME,
                SUM(l.QUANTITY) AS UNITS_SOLD,
                ROUND(SUM(l.QUANTITY * l.UNIT_PRICE), 2) AS LINE_REVENUE
           FROM UM.PM_CUSTOMER_SALE_LINE l
           JOIN UM.PM_CUSTOMER_SALE s ON s.ID = l.SALE_ID
          WHERE s.STATUS = 'COMPLETED'
          GROUP BY l.PRODUCT_NAME
          ORDER BY UNITS_SOLD DESC
       ) WHERE ROWNUM <= 10]',
    q_top_products
  );
  ins_query(
    'SEED_Q_SALES_TREND',
    'Revenue by month',
    q'[SELECT TRUNC(s.CREATED_AT, 'MM') AS MONTH_START,
               NVL(SUM(s.TOTAL_AMOUNT), 0) AS REVENUE,
               COUNT(*) AS ORDER_COUNT
          FROM UM.PM_CUSTOMER_SALE s
         WHERE s.STATUS = 'COMPLETED'
         GROUP BY TRUNC(s.CREATED_AT, 'MM')
         ORDER BY 1]',
    q_sales_trend
  );
  ins_query(
    'SEED_Q_SALES_WEEKLY',
    'Revenue by ISO week (proxy heatmap)',
    q'[SELECT TO_CHAR(TRUNC(s.CREATED_AT), 'IYYY-IW') AS ISO_WEEK,
               NVL(SUM(s.TOTAL_AMOUNT), 0) AS REVENUE
          FROM UM.PM_CUSTOMER_SALE s
         WHERE s.STATUS = 'COMPLETED'
           AND s.CREATED_AT >= TRUNC(SYSDATE) - 84
         GROUP BY TO_CHAR(TRUNC(s.CREATED_AT), 'IYYY-IW')
         ORDER BY 1]',
    q_sales_weekly
  );

  /* ---------- Queries: Stock ---------- */
  ins_query(
    'SEED_Q_STOCK_UNITS',
    'Sum of stock_quantity',
    q'[SELECT NVL(SUM(STOCK_QUANTITY), 0) AS TOTAL_UNITS FROM UM.PM_PRODUCT]',
    q_stock_units
  );
  ins_query(
    'SEED_Q_LOW_STOCK',
    'Low stock items',
    q'[SELECT ID, NAME, STOCK_QUANTITY, PRICE FROM UM.PM_PRODUCT
        WHERE STOCK_QUANTITY IS NOT NULL AND STOCK_QUANTITY > 0 AND STOCK_QUANTITY < 10
        ORDER BY STOCK_QUANTITY FETCH FIRST 50 ROWS ONLY]',
    q_low_stock
  );
  ins_query(
    'SEED_Q_OUT_STOCK',
    'Out of stock',
    q'[SELECT ID, NAME, NVL(STOCK_QUANTITY, 0) AS STOCK_QUANTITY FROM UM.PM_PRODUCT
        WHERE NVL(STOCK_QUANTITY, 0) = 0 FETCH FIRST 50 ROWS ONLY]',
    q_out_stock
  );
  ins_query(
    'SEED_Q_FAST_MOVING',
    'Fast-moving products (units in completed sales, last 90 days)',
    q'[SELECT * FROM (
         SELECT l.PRODUCT_NAME,
                SUM(l.QUANTITY) AS UNITS_SOLD
           FROM UM.PM_CUSTOMER_SALE_LINE l
           JOIN UM.PM_CUSTOMER_SALE s ON s.ID = l.SALE_ID
          WHERE s.STATUS = 'COMPLETED'
            AND s.CREATED_AT >= SYSDATE - 90
          GROUP BY l.PRODUCT_NAME
          ORDER BY UNITS_SOLD DESC
       ) WHERE ROWNUM <= 15]',
    q_fast_moving
  );
  ins_query(
    'SEED_Q_SLOW_MOVING',
    'Products with no completed sales in 90 days',
    q'[SELECT p.ID, p.NAME
         FROM UM.PM_PRODUCT p
        WHERE NOT EXISTS (
              SELECT 1
                FROM UM.PM_CUSTOMER_SALE_LINE l
                JOIN UM.PM_CUSTOMER_SALE s ON s.ID = l.SALE_ID
               WHERE l.PRODUCT_ID = p.ID
                 AND s.STATUS = 'COMPLETED'
                 AND s.CREATED_AT >= SYSDATE - 90
            )
        ORDER BY p.NAME
        FETCH FIRST 15 ROWS ONLY]',
    q_slow_moving
  );
  ins_query(
    'SEED_Q_STOCK_VALUE',
    'Inventory value (price * stock)',
    q'[SELECT ROUND(SUM(NVL(PRICE, 0) * NVL(STOCK_QUANTITY, 0)), 2) AS STOCK_VALUE FROM UM.PM_PRODUCT]',
    q_stock_value
  );
  ins_query(
    'SEED_Q_CAT_DIST',
    'Product count (placeholder — add CATEGORY column to pm_product to split)',
    q'[SELECT '(uncategorized)' AS CATEGORY_BUCKET, COUNT(*) AS PRODUCT_COUNT FROM UM.PM_PRODUCT]',
    q_cat_dist
  );

  /* ---------- Queries: UM ---------- */
  ins_query(
    'SEED_Q_USERS_TOTAL',
    'Total users',
    q'[SELECT COUNT(*) AS USER_COUNT FROM UM.UM_USER]',
    q_users_tot
  );
  ins_query(
    'SEED_Q_SESS_TODAY',
    'Distinct users with session activity today',
    q'[SELECT COUNT(DISTINCT USER_ID) AS ACTIVE_USERS FROM UM.UM_USER_SESSION WHERE TRUNC(CREATED_AT) = TRUNC(SYSDATE)]',
    q_sess_today
  );
  ins_query(
    'SEED_Q_LOCKED',
    'Locked accounts',
    q'[SELECT COUNT(*) AS LOCKED_USERS FROM UM.UM_USER WHERE NVL(ACCOUNT_LOCKED, 0) = 1]',
    q_locked
  );
  ins_query(
    'SEED_Q_ROLES_DIST',
    'Users per role',
    q'[SELECT r.NAME AS ROLE_NAME, COUNT(*) AS USER_COUNT
         FROM UM.UM_USER_ROLE ur
         JOIN UM.UM_ROLE r ON r.ID = ur.ROLE_ID
        GROUP BY r.NAME
        ORDER BY USER_COUNT DESC]',
    q_roles_dist
  );
  ins_query(
    'SEED_Q_AUDIT_DAY',
    'Audit events today',
    q'[SELECT COUNT(*) AS AUDIT_EVENTS_TODAY FROM UM.UM_AUDIT_LOG WHERE TRUNC(CREATED_AT) = TRUNC(SYSDATE)]',
    q_audit_day_cnt
  );
  ins_query(
    'SEED_Q_AUDIT_MONTH',
    'Audit events this month',
    q'[SELECT COUNT(*) AS AUDIT_EVENTS_MONTH FROM UM.UM_AUDIT_LOG WHERE CREATED_AT >= TRUNC(SYSDATE, 'MM')]',
    q_audit_month_cnt
  );
  ins_query(
    'SEED_Q_LOGIN_HOURS',
    'Audit events by hour today',
    q'[SELECT TO_CHAR(CREATED_AT, 'HH24') AS HR, COUNT(*) AS EVENTS
         FROM UM.UM_AUDIT_LOG
        WHERE TRUNC(CREATED_AT) = TRUNC(SYSDATE)
        GROUP BY TO_CHAR(CREATED_AT, 'HH24')
        ORDER BY 1]',
    q_login_hours
  );

  /* ---------- Queries: Audit security ---------- */
  ins_query(
    'SEED_Q_AUDIT_ROWS_TODAY',
    'Audit rows written today',
    q'[SELECT COUNT(*) AS ROWS_TODAY FROM UM.UM_AUDIT_LOG WHERE TRUNC(CREATED_AT) = TRUNC(SYSDATE)]',
    q_audit_rows_today
  );
  ins_query(
    'SEED_Q_ENDPOINTS',
    'Most requested paths (from audit)',
    q'[SELECT REQUEST_PATH, COUNT(*) AS HITS
         FROM UM.UM_AUDIT_LOG
        WHERE CREATED_AT >= SYSDATE - 30
          AND REQUEST_PATH IS NOT NULL
        GROUP BY REQUEST_PATH
        ORDER BY HITS DESC
        FETCH FIRST 20 ROWS ONLY]',
    q_endpoints
  );
  ins_query(
    'SEED_Q_FAILED_LOGIN_USERS',
    'Users with failed login attempts',
    q'[SELECT USERNAME, NVL(FAILED_LOGIN_ATTEMPTS, 0) AS FAILED_LOGIN_ATTEMPTS
         FROM UM.UM_USER
        WHERE NVL(FAILED_LOGIN_ATTEMPTS, 0) > 0
        ORDER BY FAILED_LOGIN_ATTEMPTS DESC
        FETCH FIRST 25 ROWS ONLY]',
    q_failed_users
  );
  ins_query(
    'SEED_Q_ROLE_SWITCH',
    'Sessions grouped by active_role_name',
    q'[SELECT NVL(ACTIVE_ROLE_NAME, '(none)') AS ACTIVE_ROLE_NAME, COUNT(*) AS SESSIONS
         FROM UM.UM_USER_SESSION
        GROUP BY ACTIVE_ROLE_NAME
        ORDER BY SESSIONS DESC]',
    q_role_switch
  );
  ins_query(
    'SEED_Q_AUDIT_USER_RANK',
    'Most active users (audit, 30 days)',
    q'[SELECT USERNAME, COUNT(*) AS ACTIONS
         FROM UM.UM_AUDIT_LOG
        WHERE CREATED_AT >= SYSDATE - 30 AND USERNAME IS NOT NULL
        GROUP BY USERNAME
        ORDER BY ACTIONS DESC
        FETCH FIRST 20 ROWS ONLY]',
    q_audit_user_rank
  );
  ins_query(
    'SEED_Q_AUDIT_HOURLY',
    'Audit volume by hour (7 days)',
    q'[SELECT TO_CHAR(TRUNC(CREATED_AT), 'YYYY-MM-DD') AS DAY_, TO_CHAR(CREATED_AT, 'HH24') AS HR, COUNT(*) AS CNT
         FROM UM.UM_AUDIT_LOG
        WHERE CREATED_AT >= SYSDATE - 7
        GROUP BY TRUNC(CREATED_AT), TO_CHAR(CREATED_AT, 'HH24')
        ORDER BY 1, 2]',
    q_audit_hourly
  );

  /* ---------- Dashboard 1: Customer overview ---------- */
  INSERT INTO UM.SETTINGS_DASHBOARD (NAME, SLUG, DESCRIPTION, IS_BUILTIN)
  VALUES (
    'Customer Overview',
    'customer-overview',
    'KYC metrics: totals, acquisition, status split, top buyers, monthly signups.',
    1
  ) RETURNING ID INTO d_cust;

  ins_widget(d_cust, 'KPI', 'Total customers', q_cust_total, NULL, 0, 0, 2, 1, 0);
  ins_widget(d_cust, 'KPI', 'New today', q_cust_new_day, NULL, 2, 0, 2, 1, 1);
  ins_widget(d_cust, 'KPI', 'New (7 days)', q_cust_new_week, NULL, 4, 0, 2, 1, 2);
  ins_widget(d_cust, 'KPI', 'New (month)', q_cust_new_month, NULL, 6, 0, 2, 1, 3);
  ins_widget(d_cust, 'TABLE', 'Customer status (donut substitute)', q_cust_status, NULL, 0, 1, 4, 2, 4);
  ins_widget(d_cust, 'TABLE', 'Customer acquisition trend', q_cust_growth, NULL, 4, 1, 4, 2, 5);
  ins_widget(d_cust, 'TABLE', 'Top 10 customers by purchases', q_cust_top10, NULL, 0, 3, 8, 2, 6);

  INSERT INTO UM.SETTINGS_DASH_ROLE_GRANT (DASHBOARD_ID, ROLE_TYPE) VALUES (d_cust, v_admin_type);

  /* ---------- Dashboard 2: Payment & sales ---------- */
  INSERT INTO UM.SETTINGS_DASHBOARD (NAME, SLUG, DESCRIPTION, IS_BUILTIN)
  VALUES (
    'Payment & Sales Overview',
    'payment-sales-overview',
    'Sales KPIs, order status mix, top SKUs, trends, weekly revenue buckets.',
    1
  ) RETURNING ID INTO d_pay;

  ins_widget(d_pay, 'KPI', 'Sales today', q_sale_day, NULL, 0, 0, 2, 1, 0);
  ins_widget(d_pay, 'KPI', 'Sales (7 days)', q_sale_week, NULL, 2, 0, 2, 1, 1);
  ins_widget(d_pay, 'KPI', 'Sales (MTD)', q_sale_month, NULL, 4, 0, 2, 1, 2);
  ins_widget(d_pay, 'KPI', 'Sales (YTD)', q_sale_year, NULL, 6, 0, 2, 1, 3);
  ins_widget(d_pay, 'KPI', 'Avg order value', q_aov, NULL, 8, 0, 2, 1, 4);
  ins_widget(d_pay, 'TABLE', 'Orders by status', q_pay_breakdown, NULL, 0, 1, 4, 2, 5);
  ins_widget(d_pay, 'TABLE', 'Sales trend by month', q_sales_trend, NULL, 4, 1, 4, 2, 6);
  ins_widget(d_pay, 'TABLE', 'Top selling products', q_top_products, NULL, 0, 3, 4, 2, 7);
  ins_widget(d_pay, 'TABLE', 'Weekly revenue (heatmap substitute)', q_sales_weekly, NULL, 4, 3, 4, 2, 8);

  INSERT INTO UM.SETTINGS_DASH_ROLE_GRANT (DASHBOARD_ID, ROLE_TYPE) VALUES (d_pay, v_admin_type);

  /* ---------- Dashboard 3: Stock ---------- */
  INSERT INTO UM.SETTINGS_DASHBOARD (NAME, SLUG, DESCRIPTION, IS_BUILTIN)
  VALUES (
    'Stock & Inventory',
    'stock-inventory',
    'Stock units, valuation, low/out lists, movement proxies.',
    1
  ) RETURNING ID INTO d_stock;

  ins_widget(d_stock, 'KPI', 'Total units in stock', q_stock_units, NULL, 0, 0, 2, 1, 0);
  ins_widget(d_stock, 'KPI', 'Inventory value', q_stock_value, NULL, 2, 0, 2, 1, 1);
  ins_widget(d_stock, 'TABLE', 'Low stock alerts', q_low_stock, NULL, 0, 1, 4, 2, 2);
  ins_widget(d_stock, 'TABLE', 'Out of stock', q_out_stock, NULL, 4, 1, 4, 2, 3);
  ins_widget(d_stock, 'TABLE', 'Fast-moving (90d)', q_fast_moving, NULL, 0, 3, 4, 2, 4);
  ins_widget(d_stock, 'TABLE', 'Slow / no movement (90d)', q_slow_moving, NULL, 4, 3, 4, 2, 5);
  ins_widget(d_stock, 'TABLE', 'Category distribution (placeholder)', q_cat_dist, NULL, 0, 5, 8, 2, 6);

  INSERT INTO UM.SETTINGS_DASH_ROLE_GRANT (DASHBOARD_ID, ROLE_TYPE) VALUES (d_stock, v_admin_type);

  /* ---------- Dashboard 4: UM activity ---------- */
  INSERT INTO UM.SETTINGS_DASHBOARD (NAME, SLUG, DESCRIPTION, IS_BUILTIN)
  VALUES (
    'User Management & Activity',
    'user-management-activity',
    'User counts, sessions, locks, role distribution, audit volume, hourly activity.',
    1
  ) RETURNING ID INTO d_um;

  ins_widget(d_um, 'KPI', 'Total users', q_users_tot, NULL, 0, 0, 2, 1, 0);
  ins_widget(d_um, 'KPI', 'Active users today (sessions)', q_sess_today, NULL, 2, 0, 2, 1, 1);
  ins_widget(d_um, 'KPI', 'Locked users', q_locked, NULL, 4, 0, 2, 1, 2);
  ins_widget(d_um, 'KPI', 'Audit events today', q_audit_day_cnt, NULL, 6, 0, 2, 1, 3);
  ins_widget(d_um, 'KPI', 'Audit events (MTD)', q_audit_month_cnt, NULL, 8, 0, 2, 1, 4);
  ins_widget(d_um, 'TABLE', 'Role distribution', q_roles_dist, NULL, 0, 1, 4, 2, 5);
  ins_widget(d_um, 'TABLE', 'Login / audit activity by hour', q_login_hours, NULL, 4, 1, 4, 2, 6);

  INSERT INTO UM.SETTINGS_DASH_ROLE_GRANT (DASHBOARD_ID, ROLE_TYPE) VALUES (d_um, v_admin_type);

  /* ---------- Dashboard 5: Audit & security ---------- */
  INSERT INTO UM.SETTINGS_DASHBOARD (NAME, SLUG, DESCRIPTION, IS_BUILTIN)
  VALUES (
    'Audit & Security Monitoring',
    'audit-security-monitoring',
    'Audit throughput, hot endpoints, risky accounts, role-switch signals.',
    1
  ) RETURNING ID INTO d_audit;

  ins_widget(d_audit, 'KPI', 'Audit rows today', q_audit_rows_today, NULL, 0, 0, 2, 1, 0);
  ins_widget(d_audit, 'TABLE', 'Most used endpoints', q_endpoints, NULL, 0, 1, 4, 2, 1);
  ins_widget(d_audit, 'TABLE', 'Failed login attempts by user', q_failed_users, NULL, 4, 1, 4, 2, 2);
  ins_widget(d_audit, 'TABLE', 'Sessions by active role', q_role_switch, NULL, 0, 3, 4, 2, 3);
  ins_widget(d_audit, 'TABLE', 'Most active users', q_audit_user_rank, NULL, 4, 3, 4, 2, 4);
  ins_widget(d_audit, 'TABLE', 'Audit timeline (hour × day)', q_audit_hourly, NULL, 0, 5, 8, 2, 5);

  INSERT INTO UM.SETTINGS_DASH_ROLE_GRANT (DASHBOARD_ID, ROLE_TYPE) VALUES (d_audit, v_admin_type);

  /* ---------- Dashboard 6: Quick actions ---------- */
  INSERT INTO UM.SETTINGS_DASHBOARD (NAME, SLUG, DESCRIPTION, IS_BUILTIN)
  VALUES (
    'Quick Actions Panel',
    'quick-actions-panel',
    'Shortcuts to common admin screens.',
    1
  ) RETURNING ID INTO d_quick;

  ins_widget(
    d_quick,
    'QUICK_ACTION',
    'Shortcuts',
    NULL,
    '{"actions":[{"label":"Add New Customer","route":"/kyc/customers"},{"label":"Add New Product","route":"/pm/products"},{"label":"Add Payment","route":"/pay"},{"label":"Manage Users","route":"/um/user"},{"label":"Audit Log","route":"/um/audit"},{"label":"Roles & Permissions","route":"/um/role"}]}',
    0,
    0,
    8,
    2,
    0
  );

  INSERT INTO UM.SETTINGS_DASH_ROLE_GRANT (DASHBOARD_ID, ROLE_TYPE) VALUES (d_quick, v_admin_type);

  COMMIT;
END;
/

-- UM.UM_ROLE.NAME values like 'admin' match Spring ROLE_ADMIN after normalization (ADMIN).
-- Optional: duplicate grants using names from UM.UM_ROLE (see script header).
