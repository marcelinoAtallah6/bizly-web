-- =============================================================================
-- Travel Operations dashboard (dashboard builder)
-- Slug: travel-operations
-- Grants: any UM_ROLE whose name contains TRAVEL (ROLE_TYPE required)
--
-- Prerequisites:
--   UM travel_* tables (travel-schema-oracle.sql + patches)
--   UM.SETTINGS_* DDL (01-settings-ddl.sql)
--   SettingsWidgetDataService binds :business_id for tenant-scoped SQL
--
-- Runtime: KPI widgets use REFRESH_SEC = 60 (auto-refresh in Angular dashboard).
-- =============================================================================

SET DEFINE OFF;

BEGIN
  FOR r IN (
    SELECT ID FROM UM.SETTINGS_DASHBOARD WHERE SLUG = 'travel-operations'
  ) LOOP
    DELETE FROM UM.SETTINGS_WIDGET WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_DASH_ROLE_GRANT WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_DASH_USER_GRANT WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_NAV_PREF WHERE DASHBOARD_ID = r.ID;
    DELETE FROM UM.SETTINGS_DASHBOARD WHERE ID = r.ID;
  END LOOP;
  DELETE FROM UM.SETTINGS_QUERY_DEF WHERE NAME LIKE 'TRAVEL_Q_%';
END;
/

DECLARE
  q_active_bookings   NUMBER;
  q_revenue_month     NUMBER;
  q_departures_7d     NUMBER;
  q_outstanding_inv   NUMBER;
  q_pending_visas     NUMBER;
  q_followups_today   NUMBER;
  q_calendar_events   NUMBER;
  q_upcoming_depart   NUMBER;
  q_followups_cards   NUMBER;
  q_visa_pipeline     NUMBER;

  d_travel            NUMBER;
  v_travel_role_type  NUMBER;
  v_cfg_cal           CLOB;
  v_cfg_cards         CLOB;
  v_cfg_table         CLOB;

  PROCEDURE ins_query(p_name VARCHAR2, p_desc VARCHAR2, p_sql CLOB, p_id OUT NUMBER) IS
  BEGIN
    INSERT INTO UM.SETTINGS_QUERY_DEF (NAME, DESCRIPTION, SQL_TEXT)
    VALUES (p_name, p_desc, p_sql)
    RETURNING ID INTO p_id;
  END;

  PROCEDURE ins_widget(
    p_dash    NUMBER,
    p_type    VARCHAR2,
    p_title   VARCHAR2,
    p_qid     NUMBER,
    p_cfg     CLOB,
    p_x       NUMBER,
    p_y       NUMBER,
    p_w       NUMBER,
    p_h       NUMBER,
    p_ord     NUMBER,
    p_refresh NUMBER DEFAULT NULL
  ) IS
  BEGIN
    INSERT INTO UM.SETTINGS_WIDGET (
      DASHBOARD_ID, WIDGET_TYPE, TITLE, QUERY_DEF_ID, CONFIG_JSON,
      GRID_X, GRID_Y, GRID_W, GRID_H, SORT_ORDER, REFRESH_SEC
    ) VALUES (
      p_dash, p_type, p_title, p_qid, p_cfg,
      p_x, p_y, p_w, p_h, p_ord, p_refresh
    );
  END;

BEGIN
  v_cfg_cal := q'[{"dateField":"EVENT_DATE","titleField":"TITLE","typeField":"EVENT_TYPE","routeField":"ROUTE","idField":"BOOKING_ID","bookingRouteBase":"/travel/bookings","travelOnlyFilter":true,"travelEventTypes":["DEPARTURE","RETURN","FOLLOW_UP","PAYMENT"],"colorMap":{"DEPARTURE":"#5D87FF","RETURN":"#49BEFF","FOLLOW_UP":"#FFAE1F","PAYMENT":"#13DEB9","DEFAULT":"#539BFF"}}]';
  v_cfg_cards := q'[{"titleField":"CLIENT_NAME","subtitleField":"FOLLOW_UP_TYPE","channelField":"CHANNEL","metaField":"NOTES","actionLabel":"Send Now","actionRoute":"/travel/follow-ups","routeIdField":"BOOKING_ID"}]';
  v_cfg_table := q'[{"linkRoute":"/travel/bookings","linkLabel":"View"}]';

  BEGIN
    SELECT ROLE_TYPE INTO v_travel_role_type
      FROM UM.UM_ROLE
     WHERE ROLE_TYPE IS NOT NULL
       AND (
         UPPER(TRIM(NAME)) IN ('TRAVEL OWNER', 'TRAVEL_OWNER', 'TRAVEL AGENCY', 'TRAVEL')
         OR UPPER(TRIM(NAME)) LIKE '%TRAVEL%'
       )
       AND ROWNUM = 1;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      v_travel_role_type := NULL;
  END;

  /* ---------- Queries ---------- */
  ins_query(
    'TRAVEL_Q_ACTIVE_BOOKINGS',
    'Active bookings (ENQUIRY, QUOTED, CONFIRMED)',
    q'[SELECT COUNT(*) AS ACTIVE_BOOKINGS
         FROM UM.TRAVEL_BOOKING b
        WHERE b.BUSINESS_ID = :business_id
          AND b.STATUS IN ('ENQUIRY', 'QUOTED', 'CONFIRMED')]',
    q_active_bookings
  );

  ins_query(
    'TRAVEL_Q_REVENUE_MONTH',
    'Payments received this calendar month',
    q'[SELECT NVL(SUM(p.AMOUNT), 0) AS REVENUE_MONTH
         FROM UM.TRAVEL_PAYMENT p
        WHERE p.BUSINESS_ID = :business_id
          AND p.PAID_AT >= TRUNC(SYSDATE, 'MM')]',
    q_revenue_month
  );

  ins_query(
    'TRAVEL_Q_DEPARTURES_7D',
    'Bookings departing within 7 days',
    q'[SELECT COUNT(*) AS DEPARTURES_7D
         FROM UM.TRAVEL_BOOKING b
        WHERE b.BUSINESS_ID = :business_id
          AND b.DEPARTURE_DATE BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 7]',
    q_departures_7d
  );

  ins_query(
    'TRAVEL_Q_OUTSTANDING_INVOICES',
    'Overdue / partial invoices — count and balance',
    q'[SELECT COUNT(*) || ' · $' || TO_CHAR(
             NVL(SUM(
               i.AMOUNT - NVL((
                 SELECT SUM(p.AMOUNT) FROM UM.TRAVEL_PAYMENT p WHERE p.INVOICE_ID = i.ID
               ), 0)
             ), 0),
             'FM999,999,990.00'
           ) AS OUTSTANDING
         FROM UM.TRAVEL_INVOICE i
        WHERE i.BUSINESS_ID = :business_id
          AND i.STATUS IN ('OVERDUE', 'PARTIAL')]',
    q_outstanding_inv
  );

  ins_query(
    'TRAVEL_Q_PENDING_VISAS',
    'Visa applications in progress',
    q'[SELECT COUNT(*) AS PENDING_VISAS
         FROM UM.TRAVEL_VISA_APPLICATION v
        WHERE v.BUSINESS_ID = :business_id
          AND v.STATUS IN ('PENDING', 'SUBMITTED', 'UNDER_REVIEW')]',
    q_pending_visas
  );

  ins_query(
    'TRAVEL_Q_FOLLOWUPS_TODAY',
    'Open follow-ups due today',
    q'[SELECT COUNT(*) AS FOLLOWUPS_TODAY
         FROM UM.TRAVEL_FOLLOW_UP f
        WHERE f.BUSINESS_ID = :business_id
          AND TRUNC(f.DUE_AT) = TRUNC(SYSDATE)
          AND f.STATUS IN ('OPEN', 'IN_PROGRESS')]',
    q_followups_today
  );

  ins_query(
    'TRAVEL_Q_CALENDAR_EVENTS',
    'Unified travel calendar feed',
    q'[SELECT EVENT_DATE, TITLE, EVENT_TYPE, ROUTE, BOOKING_ID FROM (
          SELECT TO_CHAR(TRUNC(b.DEPARTURE_DATE), 'YYYY-MM-DD') AS EVENT_DATE,
                 b.REFERENCE_NO || ' · ' || c.FULL_NAME AS TITLE,
                 'DEPARTURE' AS EVENT_TYPE,
                 '/travel/bookings' AS ROUTE,
                 b.ID AS BOOKING_ID
            FROM UM.TRAVEL_BOOKING b
            JOIN UM.TRAVEL_CLIENT c ON c.ID = b.CLIENT_ID
           WHERE b.BUSINESS_ID = :business_id
             AND b.DEPARTURE_DATE IS NOT NULL
          UNION ALL
          SELECT TO_CHAR(TRUNC(b.RETURN_DATE), 'YYYY-MM-DD'),
                 b.REFERENCE_NO || ' · return',
                 'RETURN',
                 '/travel/bookings',
                 b.ID
            FROM UM.TRAVEL_BOOKING b
           WHERE b.BUSINESS_ID = :business_id
             AND b.RETURN_DATE IS NOT NULL
          UNION ALL
          SELECT TO_CHAR(TRUNC(f.DUE_AT), 'YYYY-MM-DD'),
                 NVL(f.SUBJECT, 'Follow-up'),
                 'FOLLOW_UP',
                 '/travel/follow-ups',
                 f.BOOKING_ID
            FROM UM.TRAVEL_FOLLOW_UP f
           WHERE f.BUSINESS_ID = :business_id
             AND f.DUE_AT IS NOT NULL
          UNION ALL
          SELECT TO_CHAR(TRUNC(i.DUE_AT), 'YYYY-MM-DD'),
                 NVL(i.INVOICE_NO, 'Invoice') || ' due',
                 'PAYMENT',
                 '/travel/finance',
                 i.BOOKING_ID
            FROM UM.TRAVEL_INVOICE i
           WHERE i.BUSINESS_ID = :business_id
             AND i.DUE_AT IS NOT NULL
             AND i.STATUS IN ('ISSUED', 'PARTIAL', 'OVERDUE')
        )
        ORDER BY EVENT_DATE]',
    q_calendar_events
  );

  ins_query(
    'TRAVEL_Q_UPCOMING_DEPARTURES',
    'Departures in the next 7 days (list)',
    q'[SELECT b.REFERENCE_NO AS BOOKING_REF,
               c.FULL_NAME AS CLIENT_NAME,
               NVL(p.DESTINATION, '—') AS DESTINATION,
               TO_CHAR(b.DEPARTURE_DATE, 'YYYY-MM-DD') AS TRAVEL_DATE,
               NVL((
                 SELECT COUNT(*) FROM UM.TRAVEL_BOOKING_PASSENGER bp
                  WHERE bp.BOOKING_ID = b.ID
               ), 0) AS PAX,
               b.STATUS AS STATUS
          FROM UM.TRAVEL_BOOKING b
          JOIN UM.TRAVEL_CLIENT c ON c.ID = b.CLIENT_ID
          LEFT JOIN UM.TRAVEL_PACKAGE p ON p.ID = b.PACKAGE_ID
         WHERE b.BUSINESS_ID = :business_id
           AND b.DEPARTURE_DATE BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 7
         ORDER BY b.DEPARTURE_DATE]',
    q_upcoming_depart
  );

  ins_query(
    'TRAVEL_Q_FOLLOWUPS_CARDS',
    'Follow-ups due today (card list)',
    q'[SELECT c.FULL_NAME AS CLIENT_NAME,
               f.SUBJECT AS FOLLOW_UP_TYPE,
               CASE
                 WHEN UPPER(f.SUBJECT) LIKE '%CALL%' THEN 'Call'
                 WHEN UPPER(f.SUBJECT) LIKE '%EMAIL%' OR UPPER(f.SUBJECT) LIKE '%MAIL%' THEN 'Email'
                 WHEN UPPER(f.SUBJECT) LIKE '%WHATSAPP%' THEN 'WhatsApp'
                 ELSE 'Email'
               END AS CHANNEL,
               f.NOTES AS NOTES,
               f.BOOKING_ID AS BOOKING_ID
          FROM UM.TRAVEL_FOLLOW_UP f
          LEFT JOIN UM.TRAVEL_CLIENT c ON c.ID = f.CLIENT_ID
         WHERE f.BUSINESS_ID = :business_id
           AND TRUNC(f.DUE_AT) = TRUNC(SYSDATE)
           AND f.STATUS IN ('OPEN', 'IN_PROGRESS')
         ORDER BY f.DUE_AT]',
    q_followups_cards
  );

  ins_query(
    'TRAVEL_Q_VISA_PIPELINE',
    'Visa applications by status',
    q'[SELECT v.STATUS AS STATUS, COUNT(*) AS CNT
         FROM UM.TRAVEL_VISA_APPLICATION v
        WHERE v.BUSINESS_ID = :business_id
        GROUP BY v.STATUS
        ORDER BY CNT DESC]',
    q_visa_pipeline
  );

  /* ---------- Dashboard ---------- */
  INSERT INTO UM.SETTINGS_DASHBOARD (NAME, SLUG, DESCRIPTION, IS_BUILTIN)
  VALUES (
    'Travel Operations',
    'travel-operations',
    'Travel agency KPIs, calendar, departures, follow-ups, and visa pipeline.',
    1
  ) RETURNING ID INTO d_travel;

  /* KPI row — refresh every 60 seconds */
  ins_widget(d_travel, 'KPI', 'Active bookings', q_active_bookings, NULL, 0, 0, 2, 2, 0, 60);
  ins_widget(d_travel, 'KPI', 'Revenue this month', q_revenue_month, NULL, 2, 0, 2, 2, 1, 60);
  ins_widget(d_travel, 'KPI', 'Upcoming departures (7d)', q_departures_7d, NULL, 4, 0, 2, 2, 2, 60);
  ins_widget(d_travel, 'KPI', 'Outstanding invoices', q_outstanding_inv, NULL, 6, 0, 2, 2, 3, 60);
  ins_widget(d_travel, 'KPI', 'Pending visas', q_pending_visas, NULL, 8, 0, 2, 2, 4, 60);
  ins_widget(d_travel, 'KPI', 'Follow-ups due today', q_followups_today, NULL, 10, 0, 2, 2, 5, 60);

  /* Calendar — full width under KPIs */
  ins_widget(d_travel, 'CALENDAR', 'Operations calendar', q_calendar_events, v_cfg_cal, 0, 2, 12, 5, 6, NULL);

  /* Three-column row */
  ins_widget(d_travel, 'TABLE', 'Upcoming departures', q_upcoming_depart, v_cfg_table, 0, 7, 4, 4, 7, NULL);
  ins_widget(d_travel, 'CARD_LIST', 'Follow-ups due today', q_followups_cards, v_cfg_cards, 4, 7, 4, 4, 8, NULL);
  ins_widget(d_travel, 'DONUT_CHART', 'Visa pipeline', q_visa_pipeline, NULL, 8, 7, 4, 4, 9, NULL);

  IF v_travel_role_type IS NOT NULL THEN
    INSERT INTO UM.SETTINGS_DASH_ROLE_GRANT (DASHBOARD_ID, ROLE_TYPE)
    VALUES (d_travel, v_travel_role_type);
  ELSE
    DBMS_OUTPUT.PUT_LINE('[travel-dashboard] No TRAVEL role with ROLE_TYPE found — grant manually in builder.');
  END IF;
END;
/

COMMIT;

PROMPT Travel Operations dashboard seeded (slug: travel-operations).
