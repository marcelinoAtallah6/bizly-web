-- =============================================================================
-- Bizly Executive Showcase: dense analytics queries + one dashboard using ALL
-- widget types (KPI, charts, heatmap, candlestick, treemap, quick actions).
--
-- Run AFTER: 01-settings-ddl.sql, 02-seed-settings-dashboards-admin.sql (optional).
--
-- Prerequisites: UM.KYC_CUSTOMER, UM.PM_PRODUCT, UM.PM_CUSTOMER_SALE,
--                UM.PM_CUSTOMER_SALE_LINE, UM.UM_AUDIT_LOG (same as script 02).
--
-- Idempotent: removes slug bizly-executive-showcase and SEED_Q_BIZLY_% queries.
-- =============================================================================

SET DEFINE OFF;

BEGIN
  FOR r IN (
    SELECT ID FROM UM.SETTINGS_DASHBOARD WHERE SLUG = 'bizly-executive-showcase'
  ) LOOP
    DELETE FROM UM.SETTINGS_WIDGET WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_DASH_ROLE_GRANT WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_NAV_PREF WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_DASHBOARD WHERE ID = r.ID;
  END LOOP;
  DELETE FROM UM.SETTINGS_QUERY_DEF WHERE NAME LIKE 'SEED_Q_BIZLY_%';
END;
/

DECLARE
  q_kpi_rev          NUMBER;
  q_kpi_cust         NUMBER;
  q_kpi_ord          NUMBER;
  q_kpi_rev7         NUMBER;
  q_line_rev         NUMBER;
  q_area_cum         NUMBER;
  q_bar_dow          NUMBER;
  q_stack_mo         NUMBER;
  q_hbar_prod        NUMBER;
  q_pie_ord          NUMBER;
  q_donut_cust       NUMBER;
  q_polar_cat        NUMBER;
  q_radial_seg       NUMBER;
  q_radar_ch         NUMBER;
  q_scatter_ps       NUMBER;
  q_bubble_p         NUMBER;
  q_heat_mtx         NUMBER;
  q_range_px         NUMBER;
  q_candle_syn       NUMBER;
  q_tree_rev         NUMBER;
  q_tbl_sales        NUMBER;

  d_show NUMBER;
  v_admin_type NUMBER;

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
  /* KPIs */
  ins_query(
    'SEED_Q_BIZLY_KPI_REV_MTD',
    'Completed revenue month-to-date',
    q'[SELECT ROUND(NVL(SUM(TOTAL_AMOUNT), 0), 2) AS REVENUE_MTD FROM UM.PM_CUSTOMER_SALE WHERE STATUS = 'COMPLETED' AND CREATED_AT >= TRUNC(SYSDATE, 'MM')]',
    q_kpi_rev
  );
  ins_query(
    'SEED_Q_BIZLY_KPI_CUSTOMERS',
    'Registered customers (total)',
    q'[SELECT COUNT(*) AS CUSTOMER_COUNT FROM UM.KYC_CUSTOMER]',
    q_kpi_cust
  );
  ins_query(
    'SEED_Q_BIZLY_KPI_ORDER_COUNT',
    'Total orders (all statuses)',
    q'[SELECT COUNT(*) AS ORDER_COUNT FROM UM.PM_CUSTOMER_SALE]',
    q_kpi_ord
  );
  ins_query(
    'SEED_Q_BIZLY_KPI_REV_7D',
    'Completed revenue — trailing 7 days',
    q'[SELECT ROUND(NVL(SUM(TOTAL_AMOUNT), 0), 2) AS REVENUE_7D FROM UM.PM_CUSTOMER_SALE WHERE STATUS = 'COMPLETED' AND CREATED_AT >= TRUNC(SYSDATE) - 7]',
    q_kpi_rev7
  );

  /* Line: 90-day spine so chart always has points */
  ins_query(
    'SEED_Q_BIZLY_LINE_REVENUE_90D',
    'Daily revenue (completed) — 90-day calendar spine',
    q'[WITH spine AS (
         SELECT TRUNC(SYSDATE) - (LEVEL - 1) AS d FROM dual CONNECT BY LEVEL <= 90
       )
       SELECT TO_CHAR(sp.d, 'YYYY-MM-DD') AS DAY_LABEL,
              NVL(SUM(s.TOTAL_AMOUNT), 0) AS REVENUE
         FROM spine sp
         LEFT JOIN UM.PM_CUSTOMER_SALE s
           ON TRUNC(s.CREATED_AT) = sp.d AND s.STATUS = 'COMPLETED'
        GROUP BY sp.d
        ORDER BY sp.d]',
    q_line_rev
  );

  /* Area: cumulative revenue */
  ins_query(
    'SEED_Q_BIZLY_AREA_CUM_REV',
    'Cumulative revenue (completed) — trailing 90 days',
    q'[WITH spine AS (
         SELECT TRUNC(SYSDATE) - (LEVEL - 1) AS d FROM dual CONNECT BY LEVEL <= 90
       ),
       daily AS (
         SELECT sp.d AS day_,
                NVL(SUM(s.TOTAL_AMOUNT), 0) AS rev
           FROM spine sp
           LEFT JOIN UM.PM_CUSTOMER_SALE s
             ON TRUNC(s.CREATED_AT) = sp.d AND s.STATUS = 'COMPLETED'
          GROUP BY sp.d
       )
       SELECT TO_CHAR(day_, 'YYYY-MM-DD') AS DAY_LABEL,
              SUM(rev) OVER (ORDER BY day_) AS CUMULATIVE_REVENUE
         FROM daily
        ORDER BY day_]',
    q_area_cum
  );

  ins_query(
    'SEED_Q_BIZLY_BAR_WEEKDAY',
    'Revenue by weekday (completed, last 180 days)',
    q'[SELECT TO_CHAR(TRUNC(s.CREATED_AT), 'DY', 'NLS_DATE_LANGUAGE=AMERICAN') AS WEEKDAY,
              ROUND(NVL(SUM(s.TOTAL_AMOUNT), 0), 2) AS REVENUE
         FROM UM.PM_CUSTOMER_SALE s
        WHERE s.STATUS = 'COMPLETED'
          AND s.CREATED_AT >= SYSDATE - 180
        GROUP BY TO_CHAR(TRUNC(s.CREATED_AT), 'DY', 'NLS_DATE_LANGUAGE=AMERICAN'),
                 TO_CHAR(TRUNC(s.CREATED_AT), 'D')
        ORDER BY TO_CHAR(TRUNC(s.CREATED_AT), 'D')]',
    q_bar_dow
  );

  ins_query(
    'SEED_Q_BIZLY_STACK_STATUS_MONTH',
    'Monthly revenue split by order status (wide series)',
    q'[SELECT TRUNC(CREATED_AT, 'MM') AS MONTH_START,
              ROUND(SUM(CASE WHEN STATUS = 'COMPLETED' THEN NVL(TOTAL_AMOUNT, 0) ELSE 0 END), 2) AS COMPLETED_AMT,
              ROUND(SUM(CASE WHEN STATUS IN ('PENDING', 'PROCESSING', 'OPEN') THEN NVL(TOTAL_AMOUNT, 0) ELSE 0 END), 2) AS PIPELINE_AMT,
              ROUND(SUM(CASE WHEN STATUS NOT IN ('COMPLETED', 'PENDING', 'PROCESSING', 'OPEN') OR STATUS IS NULL THEN NVL(TOTAL_AMOUNT, 0) ELSE 0 END), 2) AS OTHER_AMT
         FROM UM.PM_CUSTOMER_SALE
        WHERE CREATED_AT >= ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -11)
        GROUP BY TRUNC(CREATED_AT, 'MM')
        ORDER BY 1]',
    q_stack_mo
  );

  ins_query(
    'SEED_Q_BIZLY_HBAR_TOP_PRODUCTS',
    'Top 12 products by line revenue (completed)',
    q'[SELECT * FROM (
         SELECT l.PRODUCT_NAME,
                ROUND(SUM(l.QUANTITY * NVL(l.UNIT_PRICE, 0)), 2) AS LINE_REVENUE
           FROM UM.PM_CUSTOMER_SALE_LINE l
           JOIN UM.PM_CUSTOMER_SALE s ON s.ID = l.SALE_ID
          WHERE s.STATUS = 'COMPLETED'
          GROUP BY l.PRODUCT_NAME
          ORDER BY LINE_REVENUE DESC NULLS LAST
       )
       WHERE ROWNUM <= 12]',
    q_hbar_prod
  );

  ins_query(
    'SEED_Q_BIZLY_PIE_ORDER_STATUS',
    'Order count by status',
    q'[SELECT NVL(STATUS, '(blank)') AS STATUS_NAME, COUNT(*) AS ORDER_COUNT
         FROM UM.PM_CUSTOMER_SALE
        GROUP BY STATUS]',
    q_pie_ord
  );

  ins_query(
    'SEED_Q_BIZLY_DONUT_CUST_STATUS',
    'Customers by status bucket',
    q'[SELECT NVL(CUSTOMER_STATUS, 'UNKNOWN') AS STATUS_BUCKET, COUNT(*) AS CNT
         FROM UM.KYC_CUSTOMER
        GROUP BY NVL(CUSTOMER_STATUS, 'UNKNOWN')]',
    q_donut_cust
  );

  ins_query(
    'SEED_Q_BIZLY_POLAR_PRICE_TIER',
    'Products grouped by price tier (polarArea)',
    q'[SELECT CASE
              WHEN NVL(PRICE, 0) < 25 THEN 'Under 25'
              WHEN NVL(PRICE, 0) < 100 THEN '25 - 99'
              WHEN NVL(PRICE, 0) < 500 THEN '100 - 499'
              ELSE '500+'
            END AS PRICE_TIER,
            COUNT(*) AS PRODUCT_COUNT
       FROM UM.PM_PRODUCT
      GROUP BY CASE
              WHEN NVL(PRICE, 0) < 25 THEN 'Under 25'
              WHEN NVL(PRICE, 0) < 100 THEN '25 - 99'
              WHEN NVL(PRICE, 0) < 500 THEN '100 - 499'
              ELSE '500+'
            END]',
    q_polar_cat
  );

  ins_query(
    'SEED_Q_BIZLY_RADIAL_PIPELINE',
    'Pipeline vs completed (radial — long format: label + amount)',
    q'[SELECT 'Completed revenue' AS SEGMENT_NAME,
              ROUND(NVL(SUM(TOTAL_AMOUNT), 0), 2) AS AMOUNT_VAL
         FROM UM.PM_CUSTOMER_SALE
        WHERE STATUS = 'COMPLETED'
      UNION ALL
      SELECT 'Pipeline / other',
              ROUND(NVL(SUM(TOTAL_AMOUNT), 0), 2)
         FROM UM.PM_CUSTOMER_SALE
        WHERE STATUS <> 'COMPLETED' OR STATUS IS NULL]',
    q_radial_seg
  );

  ins_query(
    'SEED_Q_BIZLY_RADAR_MIXED',
    'Multi-metric snapshot by channel (radar spokes = regions derived)',
    q'[SELECT NVL(c.COUNTRY, 'Unknown') AS REGION,
              ROUND(NVL(SUM(s.TOTAL_AMOUNT), 0), 2) AS REVENUE_K,
              COUNT(DISTINCT s.ID) AS ORDER_CNT,
              COUNT(DISTINCT c.ID) AS CUST_CNT,
              ROUND(AVG(s.TOTAL_AMOUNT), 2) AS AVG_TICKET
         FROM UM.KYC_CUSTOMER c
         LEFT JOIN UM.PM_CUSTOMER_SALE s ON s.CUSTOMER_ID = c.ID AND s.STATUS = 'COMPLETED'
        GROUP BY NVL(c.COUNTRY, 'Unknown')
       HAVING COUNT(DISTINCT c.ID) > 0 OR COUNT(DISTINCT s.ID) > 0
        FETCH FIRST 8 ROWS ONLY]',
    q_radar_ch
  );

  ins_query(
    'SEED_Q_BIZLY_SCATTER_PRICE_STOCK',
    'Product price vs stock quantity',
    q'[SELECT ROUND(NVL(PRICE, 0), 2) AS PRICE_X,
              NVL(STOCK_QUANTITY, 0) AS STOCK_Y
         FROM UM.PM_PRODUCT
        WHERE PRICE IS NOT NULL]',
    q_scatter_ps
  );

  ins_query(
    'SEED_Q_BIZLY_BUBBLE_PRODUCT',
    'Bubble: price × stock × name length proxy',
    q'[SELECT ROUND(NVL(PRICE, 0), 2) AS PRICE_X,
              NVL(STOCK_QUANTITY, 0) AS STOCK_Y,
              LEAST(120, GREATEST(12, NVL(LENGTH(NAME), 8))) AS BUBBLE_Z
         FROM UM.PM_PRODUCT]',
    q_bubble_p
  );

  ins_query(
    'SEED_Q_BIZLY_HEATMAP_SYNTH',
    'Synthetic intensity grid (guaranteed dense cells)',
    q'[SELECT 'R' || CHR(64 + MOD(LEVEL, 6) + 1) AS ROW_LABEL,
              'C' || TO_CHAR(MOD(LEVEL, 9)) AS COL_LABEL,
              MOD(LEVEL * 17, 94) AS INTENSITY
         FROM dual
       CONNECT BY LEVEL <= 54]',
    q_heat_mtx
  );

  ins_query(
    'SEED_Q_BIZLY_RANGE_PRICE_BAND',
    'SKU price low/high band (±10%)',
    q'[SELECT SUBSTR(NAME, 1, 28) AS SKU,
              ROUND(NVL(PRICE, 0) * 0.85, 2) AS PRICE_LOW,
              ROUND(NVL(PRICE, 0) * 1.15, 2) AS PRICE_HIGH
         FROM UM.PM_PRODUCT
        FETCH FIRST 18 ROWS ONLY]',
    q_range_px
  );

  ins_query(
    'SEED_Q_BIZLY_CANDLE_SYNTH',
    'Synthetic OHLC series (named OPEN_P HIGH_P LOW_P CLOSE_P)',
    q'[SELECT TO_CHAR(TRUNC(SYSDATE) - LEVEL + 1, 'YYYY-MM-DD') AS ASOF_DAY,
              48 + MOD(LEVEL * 7, 22) AS OPEN_P,
              52 + MOD(LEVEL * 11, 28) AS HIGH_P,
              40 + MOD(LEVEL * 5, 18) AS LOW_P,
              50 + MOD(LEVEL * 13, 24) AS CLOSE_P
         FROM dual
       CONNECT BY LEVEL <= 60]',
    q_candle_syn
  );

  ins_query(
    'SEED_Q_BIZLY_TREEMAP_PRODUCT_REV',
    'Treemap: revenue by product name',
    q'[SELECT * FROM (
         SELECT l.PRODUCT_NAME AS LABEL,
                ROUND(SUM(l.QUANTITY * NVL(l.UNIT_PRICE, 0)), 2) AS REVENUE_VAL
           FROM UM.PM_CUSTOMER_SALE_LINE l
           JOIN UM.PM_CUSTOMER_SALE s ON s.ID = l.SALE_ID
          WHERE s.STATUS = 'COMPLETED'
          GROUP BY l.PRODUCT_NAME
          ORDER BY REVENUE_VAL DESC NULLS LAST
       )
       WHERE ROWNUM <= 40]',
    q_tree_rev
  );

  ins_query(
    'SEED_Q_BIZLY_TABLE_RECENT_SALES',
    'Recent orders (detail)',
    q'[SELECT s.ID AS SALE_ID,
              TRUNC(s.CREATED_AT) AS SALE_DAY,
              s.STATUS,
              ROUND(NVL(s.TOTAL_AMOUNT, 0), 2) AS TOTAL_AMOUNT,
              s.CUSTOMER_ID
         FROM UM.PM_CUSTOMER_SALE s
        ORDER BY s.CREATED_AT DESC
        FETCH FIRST 120 ROWS ONLY]',
    q_tbl_sales
  );

  INSERT INTO UM.SETTINGS_DASHBOARD (NAME, SLUG, DESCRIPTION, IS_BUILTIN)
  VALUES (
    'Bizly Executive Showcase',
    'bizly-executive-showcase',
    'Customers, products & payments — every widget type with dense analytics (run script 03 after seed data exists).',
    1
  ) RETURNING ID INTO d_show;

  /* Row 0: KPIs */
  ins_widget(d_show, 'KPI', 'Revenue (MTD)', q_kpi_rev, NULL, 0, 0, 3, 1, 0);
  ins_widget(d_show, 'KPI', 'Customers', q_kpi_cust, NULL, 3, 0, 3, 1, 1);
  ins_widget(d_show, 'KPI', 'Orders (all)', q_kpi_ord, NULL, 6, 0, 3, 1, 2);
  ins_widget(d_show, 'KPI', 'Revenue (7d)', q_kpi_rev7, NULL, 9, 0, 3, 1, 3);

  /* Row 1–2: Line + Area */
  ins_widget(d_show, 'LINE_CHART', 'Daily revenue (90d)', q_line_rev, NULL, 0, 1, 8, 2, 4);
  ins_widget(d_show, 'AREA_CHART', 'Cumulative revenue', q_area_cum, NULL, 8, 1, 4, 2, 5);

  /* Row 3–4 */
  ins_widget(d_show, 'BAR_CHART', 'Revenue by weekday', q_bar_dow, NULL, 0, 3, 4, 2, 6);
  ins_widget(d_show, 'STACKED_BAR_CHART', 'Status mix by month', q_stack_mo, NULL, 4, 3, 4, 2, 7);
  ins_widget(d_show, 'HORIZONTAL_BAR_CHART', 'Top products', q_hbar_prod, NULL, 8, 3, 4, 2, 8);

  /* Row 5–6 */
  ins_widget(d_show, 'PIE_CHART', 'Orders by status', q_pie_ord, NULL, 0, 5, 4, 2, 9);
  ins_widget(d_show, 'DONUT_CHART', 'Customers by status', q_donut_cust, NULL, 4, 5, 4, 2, 10);
  ins_widget(d_show, 'POLAR_AREA_CHART', 'Products by price tier', q_polar_cat, NULL, 8, 5, 4, 2, 11);

  /* Row 7–8 */
  ins_widget(d_show, 'RADIAL_BAR_CHART', 'Completed vs rest', q_radial_seg, NULL, 0, 7, 4, 2, 12);
  ins_widget(d_show, 'RADAR_CHART', 'Metrics by region', q_radar_ch, NULL, 4, 7, 8, 2, 13);

  /* Row 9–10 */
  ins_widget(d_show, 'SCATTER_CHART', 'Price vs stock', q_scatter_ps, NULL, 0, 9, 4, 2, 14);
  ins_widget(d_show, 'BUBBLE_CHART', 'Price × stock × size', q_bubble_p, NULL, 4, 9, 4, 2, 15);
  ins_widget(d_show, 'HEATMAP', 'Synthetic grid', q_heat_mtx, NULL, 8, 9, 4, 2, 16);

  /* Row 11–12 */
  ins_widget(d_show, 'RANGE_BAR_CHART', 'Price bands', q_range_px, NULL, 0, 11, 4, 2, 17);
  ins_widget(d_show, 'CANDLESTICK_CHART', 'Synthetic OHLC', q_candle_syn, NULL, 4, 11, 4, 2, 18);
  ins_widget(d_show, 'TREEMAP_CHART', 'Revenue by product', q_tree_rev, NULL, 8, 11, 4, 2, 19);

  /* Table + quick actions */
  ins_widget(d_show, 'TABLE', 'Recent sales rows', q_tbl_sales, NULL, 0, 13, 12, 3, 20);
  ins_widget(
    d_show,
    'QUICK_ACTION',
    'Shortcuts',
    NULL,
    '{"actions":[{"label":"Customers","route":"/kyc/customers"},{"label":"Products","route":"/pm/products"},{"label":"Payments","route":"/pay"},{"label":"Dashboard Builder","route":"/dash"},{"label":"Query Builder","route":"/qbe"}]}',
    0,
    16,
    12,
    2,
    21
  );

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
  INSERT INTO UM.SETTINGS_DASH_ROLE_GRANT (DASHBOARD_ID, ROLE_TYPE) VALUES (d_show, v_admin_type);

  COMMIT;
END;
/
