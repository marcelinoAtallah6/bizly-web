-- =============================================================================
-- Travel agency reports for Report Builder (/reports viewer)
-- Grants: UM_ROLE named "Travel" (or containing TRAVEL) via SETTINGS_REPORT_ROLE_GRANT
--
-- Prerequisites: travel_* tables, SETTINGS_REPORT*, patch-business-scope (business_id)
-- Run as UM user. Idempotent by report code (TRAVEL_*).
--
-- Note: Report Builder renders filter bar + AG-Grid + CSV export. Chart/KPI views
-- belong on the Travel Operations dashboard; use these reports for tabular export.
-- =============================================================================

SET DEFINE OFF;

DECLARE
  v_travel_role_type NUMBER;
  v_report_id        NUMBER;
  q_id               NUMBER;
  f                  CLOB;
  c                  CLOB;

  PROCEDURE upsert_query(p_name VARCHAR2, p_desc VARCHAR2, p_sql CLOB, p_id OUT NUMBER) IS
  BEGIN
    SELECT MAX(id) INTO p_id FROM um.settings_query_def WHERE UPPER(TRIM(name)) = UPPER(p_name);
    IF p_id IS NULL THEN
      INSERT INTO um.settings_query_def (name, description, sql_text, created_at, updated_at, created_by)
      VALUES (p_name, p_desc, p_sql, SYSTIMESTAMP, SYSTIMESTAMP, 'system')
      RETURNING id INTO p_id;
    ELSE
      UPDATE um.settings_query_def
         SET description = p_desc, sql_text = p_sql, updated_at = SYSTIMESTAMP
       WHERE id = p_id;
    END IF;
  END;

  PROCEDURE upsert_report(
    p_code VARCHAR2, p_name VARCHAR2, p_desc VARCHAR2, p_icon VARCHAR2,
    p_qid NUMBER, p_filters CLOB, p_columns CLOB,
    p_sort_key VARCHAR2, p_sort_dir VARCHAR2, p_sort_order NUMBER,
    p_report_id OUT NUMBER
  ) IS
  BEGIN
    MERGE INTO um.settings_report r
    USING (SELECT p_code AS code FROM dual) src
       ON (r.code = src.code)
     WHEN MATCHED THEN
       UPDATE SET name = p_name, description = p_desc, icon = p_icon, status = 'ACTIVE',
         query_def_id = p_qid, filters_json = p_filters, columns_json = p_columns,
         default_sort_key = p_sort_key, default_sort_dir = p_sort_dir, sort_order = p_sort_order,
         updated_at = SYSTIMESTAMP, updated_by = 'system'
     WHEN NOT MATCHED THEN
       INSERT (code, name, description, icon, status, query_def_id,
               filters_json, columns_json, default_sort_key, default_sort_dir,
               sort_order, created_at, updated_at, created_by, updated_by)
       VALUES (p_code, p_name, p_desc, p_icon, 'ACTIVE', p_qid,
               p_filters, p_columns, p_sort_key, p_sort_dir, p_sort_order,
               SYSTIMESTAMP, SYSTIMESTAMP, 'system', 'system');
    SELECT id INTO p_report_id FROM um.settings_report WHERE code = p_code;
  END;

  PROCEDURE grant_travel(p_report_id NUMBER) IS
  BEGIN
    IF v_travel_role_type IS NOT NULL AND p_report_id IS NOT NULL THEN
      MERGE INTO um.settings_report_role_grant g
      USING (SELECT p_report_id AS rid, v_travel_role_type AS rt FROM dual) src
         ON (g.report_id = src.rid AND g.role_type = src.rt)
       WHEN NOT MATCHED THEN
         INSERT (report_id, role_type) VALUES (src.rid, src.rt);
    END IF;
  END;

BEGIN
  /* ----- resolve Travel role ----- */
  BEGIN
    SELECT role_type INTO v_travel_role_type
      FROM um.um_role
     WHERE role_type IS NOT NULL
       AND UPPER(TRIM(name)) = 'TRAVEL'
       AND ROWNUM = 1;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      BEGIN
        SELECT role_type INTO v_travel_role_type
          FROM um.um_role
         WHERE role_type IS NOT NULL
           AND UPPER(TRIM(name)) LIKE '%TRAVEL%'
           AND ROWNUM = 1;
      EXCEPTION
        WHEN NO_DATA_FOUND THEN v_travel_role_type := NULL;
      END;
  END;

  /* 1 — Revenue & Profit (group by) */
  upsert_query('TRAVEL_Q_REVENUE_PROFIT',
    'Revenue, cost, profit by group (consultant / destination / status / month)',
    q'~
SELECT
  GRP_LABEL,
  COUNT(*) AS BOOKINGS,
  ROUND(NVL(SUM(line_revenue), 0), 2) AS TOTAL_REVENUE,
  ROUND(NVL(SUM(line_cost), 0), 2) AS TOTAL_COST,
  ROUND(NVL(SUM(line_revenue), 0) - NVL(SUM(line_cost), 0), 2) AS PROFIT,
  CASE WHEN NVL(SUM(line_revenue), 0) > 0 THEN
    ROUND((NVL(SUM(line_revenue), 0) - NVL(SUM(line_cost), 0)) / SUM(line_revenue) * 100, 2)
  ELSE 0 END AS MARGIN_PCT
FROM (
  SELECT
    CASE NVL(:groupBy, 'MONTH')
      WHEN 'CONSULTANT' THEN NVL(b.created_by, 'Unassigned')
      WHEN 'DESTINATION' THEN NVL(p.destination, '—')
      WHEN 'BOOKING_TYPE' THEN NVL(b.status, 'UNKNOWN')
      ELSE TO_CHAR(TRUNC(b.departure_date), 'YYYY-MM')
    END AS GRP_LABEL,
    NVL(b.total_amount, 0) AS line_revenue,
    NVL(comp.TOTAL_COST, 0) AS line_cost
  FROM um.travel_booking b
  LEFT JOIN um.travel_package p ON p.id = b.package_id AND p.business_id = b.business_id
  LEFT JOIN (
    SELECT booking_id, SUM(amount) AS TOTAL_COST
      FROM um.travel_booking_component
     WHERE business_id = :business_id
     GROUP BY booking_id
  ) comp ON comp.booking_id = b.id
  WHERE b.business_id = :business_id
    AND (:fromDate IS NULL OR b.departure_date >= TRUNC(CAST(:fromDate AS TIMESTAMP)))
    AND (:toDate IS NULL OR b.departure_date < TRUNC(CAST(:toDate AS TIMESTAMP)))
) detail
GROUP BY GRP_LABEL
~', q_id);
  f := q'~[{"key":"period","label":"Departure period","type":"DATE_RANGE","fromParamName":"fromDate","toParamName":"toDate"},{"key":"groupBy","label":"Group by","type":"SELECT","paramName":"groupBy","options":[{"value":"MONTH","label":"Month"},{"value":"CONSULTANT","label":"Consultant"},{"value":"DESTINATION","label":"Destination"},{"value":"BOOKING_TYPE","label":"Booking status"}]}]~';
  c := q'~[{"key":"GRP_LABEL","label":"Group","type":"STRING","sortable":true,"visible":true},{"key":"BOOKINGS","label":"Bookings","type":"NUMBER","sortable":true,"visible":true},{"key":"TOTAL_REVENUE","label":"Total revenue","type":"MONEY","sortable":true,"visible":true},{"key":"TOTAL_COST","label":"Total cost","type":"MONEY","sortable":true,"visible":true},{"key":"PROFIT","label":"Profit","type":"MONEY","sortable":true,"visible":true},{"key":"MARGIN_PCT","label":"Margin %","type":"NUMBER","sortable":true,"visible":true}]~';
  upsert_report('travel-revenue-profit', 'Travel · Revenue & Profit', 'Revenue, cost, profit and margin by consultant, destination, booking status, or month. Export CSV from the viewer.', 'payments', q_id, f, c, 'TOTAL_REVENUE', 'DESC', 10, v_report_id);
  grant_travel(v_report_id);

  /* 2 — Bookings detail */
  upsert_query('TRAVEL_Q_BOOKINGS_DETAIL',
    'Booking lines with status filter',
    q'~
SELECT b.reference_no AS BOOKING_REF, c.full_name AS CLIENT_NAME,
       NVL(p.destination, '—') AS DESTINATION, b.status AS STATUS,
       b.departure_date AS TRAVEL_DATE, b.return_date AS RETURN_DATE,
       NVL(b.total_amount, 0) AS TOTAL_AMOUNT, b.currency AS CURRENCY,
       NVL(b.created_by, '—') AS CONSULTANT
  FROM um.travel_booking b
  JOIN um.travel_client c ON c.id = b.client_id
  LEFT JOIN um.travel_package p ON p.id = b.package_id
 WHERE b.business_id = :business_id
   AND (:fromDate IS NULL OR b.created_at >= CAST(:fromDate AS TIMESTAMP))
   AND (:toDate IS NULL OR b.created_at < CAST(:toDate AS TIMESTAMP))
   AND (:bookingStatus IS NULL OR b.status = :bookingStatus)
~', q_id);
  f := q'~[{"key":"period","label":"Created period","type":"DATE_RANGE","fromParamName":"fromDate","toParamName":"toDate"},{"key":"bookingStatus","label":"Status","type":"SELECT","paramName":"bookingStatus","options":[{"value":"ENQUIRY","label":"Enquiry"},{"value":"QUOTED","label":"Quoted"},{"value":"CONFIRMED","label":"Confirmed"},{"value":"COMPLETED","label":"Completed"},{"value":"CANCELLED","label":"Cancelled"}]}]~';
  c := q'~[{"key":"BOOKING_REF","label":"Ref","type":"STRING","sortable":true,"visible":true},{"key":"CLIENT_NAME","label":"Client","type":"STRING","sortable":true,"visible":true},{"key":"DESTINATION","label":"Destination","type":"STRING","sortable":true,"visible":true},{"key":"STATUS","label":"Status","type":"STRING","sortable":true,"visible":true},{"key":"TRAVEL_DATE","label":"Departure","type":"DATE","sortable":true,"visible":true},{"key":"RETURN_DATE","label":"Return","type":"DATE","sortable":true,"visible":true},{"key":"TOTAL_AMOUNT","label":"Amount","type":"MONEY","sortable":true,"visible":true},{"key":"CONSULTANT","label":"Consultant","type":"STRING","sortable":true,"visible":true}]~';
  upsert_report('travel-bookings', 'Travel · Bookings', 'All bookings in period with optional status filter. Pair with Bookings Summary for KPI counts.', 'event', q_id, f, c, 'TRAVEL_DATE', 'DESC', 20, v_report_id);
  grant_travel(v_report_id);

  /* 3 — Bookings summary / conversion */
  upsert_query('TRAVEL_Q_BOOKINGS_SUMMARY',
    'Booking counts and conversion by consultant',
    q'~
SELECT NVL(b.created_by, 'Unassigned') AS CONSULTANT,
       COUNT(*) AS TOTAL_BOOKINGS,
       SUM(CASE WHEN b.status IN ('CONFIRMED','COMPLETED') THEN 1 ELSE 0 END) AS CONFIRMED_OR_DONE,
       SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) AS CANCELLED,
       ROUND(CASE WHEN COUNT(*) > 0 THEN
         SUM(CASE WHEN b.status IN ('CONFIRMED','COMPLETED') THEN 1 ELSE 0 END) * 100.0 / COUNT(*)
       ELSE 0 END, 2) AS CONVERSION_PCT
  FROM um.travel_booking b
 WHERE b.business_id = :business_id
   AND (:fromDate IS NULL OR b.created_at >= CAST(:fromDate AS TIMESTAMP))
   AND (:toDate IS NULL OR b.created_at < CAST(:toDate AS TIMESTAMP))
 GROUP BY NVL(b.created_by, 'Unassigned')
~', q_id);
  f := q'~[{"key":"period","label":"Created period","type":"DATE_RANGE","fromParamName":"fromDate","toParamName":"toDate"}]~';
  c := q'~[{"key":"CONSULTANT","label":"Consultant","type":"STRING","sortable":true,"visible":true},{"key":"TOTAL_BOOKINGS","label":"Total","type":"NUMBER","sortable":true,"visible":true},{"key":"CONFIRMED_OR_DONE","label":"Confirmed/Done","type":"NUMBER","sortable":true,"visible":true},{"key":"CANCELLED","label":"Cancelled","type":"NUMBER","sortable":true,"visible":true},{"key":"CONVERSION_PCT","label":"Conversion %","type":"NUMBER","sortable":true,"visible":true}]~';
  upsert_report('travel-bookings-summary', 'Travel · Bookings Summary', 'Summary counts and conversion rate by consultant for the selected period.', 'summarize', q_id, f, c, 'TOTAL_BOOKINGS', 'DESC', 21, v_report_id);
  grant_travel(v_report_id);

  /* 4 — Consultant performance */
  upsert_query('TRAVEL_Q_CONSULTANT_PERF',
    'Consultant bookings, revenue, profit, commission',
    q'~
SELECT NVL(b.created_by, 'Unassigned') AS CONSULTANT,
       COUNT(DISTINCT b.id) AS BOOKINGS,
       ROUND(NVL(SUM(b.total_amount), 0), 2) AS REVENUE,
       ROUND(NVL(SUM(b.total_amount), 0) - NVL(SUM(comp.TOTAL_COST), 0), 2) AS PROFIT,
       ROUND(NVL(SUM(comm.COMMISSION), 0), 2) AS COMMISSION
  FROM um.travel_booking b
  LEFT JOIN (
    SELECT booking_id, SUM(amount) AS TOTAL_COST
      FROM um.travel_booking_component WHERE business_id = :business_id GROUP BY booking_id
  ) comp ON comp.booking_id = b.id
  LEFT JOIN (
    SELECT booking_id, SUM(commission_amount) AS COMMISSION
      FROM um.travel_consultant_commission WHERE business_id = :business_id GROUP BY booking_id
  ) comm ON comm.booking_id = b.id
 WHERE b.business_id = :business_id
   AND (:fromDate IS NULL OR b.departure_date >= TRUNC(CAST(:fromDate AS TIMESTAMP)))
   AND (:toDate IS NULL OR b.departure_date < TRUNC(CAST(:toDate AS TIMESTAMP)))
 GROUP BY NVL(b.created_by, 'Unassigned')
~', q_id);
  f := q'~[{"key":"period","label":"Departure month range","type":"DATE_RANGE","fromParamName":"fromDate","toParamName":"toDate"}]~';
  c := q'~[{"key":"CONSULTANT","label":"Consultant","type":"STRING","sortable":true,"visible":true},{"key":"BOOKINGS","label":"Bookings","type":"NUMBER","sortable":true,"visible":true},{"key":"REVENUE","label":"Revenue","type":"MONEY","sortable":true,"visible":true},{"key":"PROFIT","label":"Profit","type":"MONEY","sortable":true,"visible":true},{"key":"COMMISSION","label":"Commission","type":"MONEY","sortable":true,"visible":true}]~';
  upsert_report('travel-consultant-performance', 'Travel · Consultant Performance', 'Per-consultant bookings, revenue, profit and commission for the departure date range.', 'groups', q_id, f, c, 'REVENUE', 'DESC', 30, v_report_id);
  grant_travel(v_report_id);

  /* 5 — Visa pipeline list */
  upsert_query('TRAVEL_Q_VISA_PIPELINE',
    'Visa applications list',
    q'~
SELECT v.id AS VISA_ID, c.full_name AS CLIENT_NAME, v.country AS COUNTRY,
       v.visa_type AS VISA_TYPE, v.status AS STATUS,
       v.submitted_at AS SUBMITTED_AT, v.decision_at AS DECISION_AT,
       b.reference_no AS BOOKING_REF
  FROM um.travel_visa_application v
  JOIN um.travel_client c ON c.id = v.client_id
  LEFT JOIN um.travel_booking b ON b.id = v.booking_id
 WHERE v.business_id = :business_id
   AND (:visaStatus IS NULL OR v.status = :visaStatus)
~', q_id);
  f := q'~[{"key":"visaStatus","label":"Visa status","type":"SELECT","paramName":"visaStatus","options":[{"value":"PENDING","label":"Pending"},{"value":"SUBMITTED","label":"Submitted"},{"value":"UNDER_REVIEW","label":"Under review"},{"value":"APPROVED","label":"Approved"},{"value":"REJECTED","label":"Rejected"}]}]~';
  c := q'~[{"key":"CLIENT_NAME","label":"Client","type":"STRING","sortable":true,"visible":true},{"key":"COUNTRY","label":"Country","type":"STRING","sortable":true,"visible":true},{"key":"VISA_TYPE","label":"Type","type":"STRING","sortable":true,"visible":true},{"key":"STATUS","label":"Status","type":"STRING","sortable":true,"visible":true},{"key":"SUBMITTED_AT","label":"Submitted","type":"DATETIME","sortable":true,"visible":true},{"key":"DECISION_AT","label":"Decision","type":"DATETIME","sortable":true,"visible":true},{"key":"BOOKING_REF","label":"Booking","type":"STRING","sortable":true,"visible":true}]~';
  upsert_report('travel-visa-pipeline', 'Travel · Visa Pipeline', 'Visa applications with status filter. Use Visa SLA report for average stage durations.', 'badge', q_id, f, c, 'SUBMITTED_AT', 'DESC', 40, v_report_id);
  grant_travel(v_report_id);

  /* 6 — Visa SLA metrics */
  upsert_query('TRAVEL_Q_VISA_SLA',
    'Average days between visa stages',
    q'~
SELECT 'PENDING to SUBMITTED (avg days)' AS SLA_METRIC,
       ROUND(AVG(CAST(v.submitted_at AS DATE) - CAST(v.created_at AS DATE)), 1) AS AVG_DAYS
  FROM um.travel_visa_application v
 WHERE v.business_id = :business_id AND v.submitted_at IS NOT NULL
UNION ALL
SELECT 'SUBMITTED to APPROVED (avg days)',
       ROUND(AVG(CAST(v.decision_at AS DATE) - CAST(v.submitted_at AS DATE)), 1)
  FROM um.travel_visa_application v
 WHERE v.business_id = :business_id
   AND v.status = 'APPROVED' AND v.submitted_at IS NOT NULL AND v.decision_at IS NOT NULL
~', q_id);
  f := '[]';
  c := q'~[{"key":"SLA_METRIC","label":"Metric","type":"STRING","sortable":false,"visible":true},{"key":"AVG_DAYS","label":"Avg days","type":"NUMBER","sortable":true,"visible":true}]~';
  upsert_report('travel-visa-sla', 'Travel · Visa SLA', 'Average days PENDING→SUBMITTED and SUBMITTED→APPROVED (schema statuses).', 'schedule', q_id, f, c, 'SLA_METRIC', 'ASC', 41, v_report_id);
  grant_travel(v_report_id);

  /* 7 — Outstanding payments / aging */
  upsert_query('TRAVEL_Q_OUTSTANDING_PAYMENTS',
    'Unpaid and partial invoices with aging',
    q'~
SELECT i.invoice_no AS INVOICE_NO, b.reference_no AS BOOKING_REF, c.full_name AS CLIENT_NAME,
       i.status AS STATUS, i.amount AS INVOICE_AMOUNT,
       NVL(pay.PAID, 0) AS AMOUNT_PAID,
       i.amount - NVL(pay.PAID, 0) AS BALANCE_DUE,
       i.due_at AS DUE_AT,
       CASE
         WHEN i.due_at IS NULL THEN 'No due date'
         WHEN TRUNC(SYSDATE) - TRUNC(i.due_at) <= 30 THEN '0-30 days'
         WHEN TRUNC(SYSDATE) - TRUNC(i.due_at) <= 60 THEN '31-60 days'
         WHEN TRUNC(SYSDATE) - TRUNC(i.due_at) <= 90 THEN '61-90 days'
         ELSE '90+ days'
       END AS AGING_BUCKET
  FROM um.travel_invoice i
  JOIN um.travel_booking b ON b.id = i.booking_id
  JOIN um.travel_client c ON c.id = b.client_id
  LEFT JOIN (
    SELECT invoice_id, SUM(amount) AS PAID FROM um.travel_payment
     WHERE business_id = :business_id GROUP BY invoice_id
  ) pay ON pay.invoice_id = i.id
 WHERE i.business_id = :business_id
   AND i.status IN ('ISSUED', 'PARTIAL', 'OVERDUE')
   AND (i.amount - NVL(pay.PAID, 0)) > 0.01
~', q_id);
  f := '[]';
  c := q'~[{"key":"INVOICE_NO","label":"Invoice","type":"STRING","sortable":true,"visible":true},{"key":"BOOKING_REF","label":"Booking","type":"STRING","sortable":true,"visible":true},{"key":"CLIENT_NAME","label":"Client","type":"STRING","sortable":true,"visible":true},{"key":"STATUS","label":"Status","type":"STRING","sortable":true,"visible":true},{"key":"BALANCE_DUE","label":"Balance due","type":"MONEY","sortable":true,"visible":true},{"key":"DUE_AT","label":"Due","type":"DATE","sortable":true,"visible":true},{"key":"AGING_BUCKET","label":"Aging","type":"STRING","sortable":true,"visible":true}]~';
  upsert_report('travel-outstanding-payments', 'Travel · Outstanding Payments', 'Open invoices with aging buckets (0–30, 31–60, 61–90, 90+). Export for accounts.', 'account_balance', q_id, f, c, 'DUE_AT', 'ASC', 50, v_report_id);
  grant_travel(v_report_id);

  /* 8 — Upcoming travel */
  upsert_query('TRAVEL_Q_UPCOMING_TRAVEL',
    'Departures in next N days',
    q'~
SELECT b.reference_no AS BOOKING_REF, c.full_name AS CLIENT_NAME,
       NVL(p.destination, '—') AS DESTINATION, b.departure_date AS TRAVEL_DATE,
       NVL((SELECT COUNT(*) FROM um.travel_booking_passenger bp WHERE bp.booking_id = b.id), 0) AS PAX,
       NVL(vs.VISA_STATUS, '—') AS VISA_STATUS,
       CASE WHEN c.passport_no IS NOT NULL THEN 'On file' ELSE 'Missing' END AS PASSPORT_STATUS
  FROM um.travel_booking b
  JOIN um.travel_client c ON c.id = b.client_id
  LEFT JOIN um.travel_package p ON p.id = b.package_id
  LEFT JOIN (
    SELECT booking_id, MAX(status) AS VISA_STATUS
      FROM um.travel_visa_application WHERE business_id = :business_id GROUP BY booking_id
  ) vs ON vs.booking_id = b.id
 WHERE b.business_id = :business_id
   AND b.departure_date BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + NVL(:horizonDays, 30)
   AND b.status IN ('ENQUIRY','QUOTED','CONFIRMED')
~', q_id);
  f := q'~[{"key":"horizonDays","label":"Horizon (days)","type":"SELECT","paramName":"horizonDays","options":[{"value":"30","label":"Next 30 days"},{"value":"60","label":"Next 60 days"},{"value":"90","label":"Next 90 days"}]}]~';
  c := q'~[{"key":"BOOKING_REF","label":"Ref","type":"STRING","sortable":true,"visible":true},{"key":"CLIENT_NAME","label":"Client","type":"STRING","sortable":true,"visible":true},{"key":"DESTINATION","label":"Destination","type":"STRING","sortable":true,"visible":true},{"key":"TRAVEL_DATE","label":"Travel date","type":"DATE","sortable":true,"visible":true},{"key":"PAX","label":"Pax","type":"NUMBER","sortable":true,"visible":true},{"key":"VISA_STATUS","label":"Visa status","type":"STRING","sortable":true,"visible":true},{"key":"PASSPORT_STATUS","label":"Passport","type":"STRING","sortable":true,"visible":true}]~';
  upsert_report('travel-upcoming-travel', 'Travel · Upcoming Travel', 'Itinerary-style list for departures in the next 30/60/90 days.', 'flight_takeoff', q_id, f, c, 'TRAVEL_DATE', 'ASC', 60, v_report_id);
  grant_travel(v_report_id);

  /* 9 — Supplier spend */
  upsert_query('TRAVEL_Q_SUPPLIER_SPEND',
    'Spend by supplier',
    q'~
SELECT s.name AS SUPPLIER, s.supplier_type AS SUPPLIER_TYPE,
       COUNT(DISTINCT bc.booking_id) AS BOOKINGS_COUNT,
       ROUND(NVL(SUM(bc.amount), 0), 2) AS TOTAL_COST,
       ROUND(CASE WHEN COUNT(DISTINCT bc.booking_id) > 0
         THEN NVL(SUM(bc.amount), 0) / COUNT(DISTINCT bc.booking_id) ELSE 0 END, 2) AS AVG_COST_PER_BOOKING,
       CASE WHEN s.active = 1 THEN 'Active' ELSE 'Inactive' END AS RATING_NOTE
  FROM um.travel_supplier s
  LEFT JOIN um.travel_booking_component bc ON bc.supplier_id = s.id AND bc.business_id = s.business_id
  LEFT JOIN um.travel_booking b ON b.id = bc.booking_id
 WHERE s.business_id = :business_id
   AND (:fromDate IS NULL OR b.departure_date >= TRUNC(CAST(:fromDate AS TIMESTAMP)))
   AND (:toDate IS NULL OR b.departure_date < TRUNC(CAST(:toDate AS TIMESTAMP)))
   AND (:supplierType IS NULL OR s.supplier_type = :supplierType)
 GROUP BY s.id, s.name, s.supplier_type, s.active
~', q_id);
  f := q'~[{"key":"period","label":"Departure period","type":"DATE_RANGE","fromParamName":"fromDate","toParamName":"toDate"},{"key":"supplierType","label":"Supplier type","type":"SELECT","paramName":"supplierType","options":[{"value":"DMC","label":"DMC"},{"value":"AIRLINE","label":"Airline"},{"value":"HOTEL","label":"Hotel"},{"value":"INSURANCE","label":"Insurance"}]}]~';
  c := q'~[{"key":"SUPPLIER","label":"Supplier","type":"STRING","sortable":true,"visible":true},{"key":"SUPPLIER_TYPE","label":"Type","type":"STRING","sortable":true,"visible":true},{"key":"BOOKINGS_COUNT","label":"Bookings","type":"NUMBER","sortable":true,"visible":true},{"key":"TOTAL_COST","label":"Total cost","type":"MONEY","sortable":true,"visible":true},{"key":"AVG_COST_PER_BOOKING","label":"Avg / booking","type":"MONEY","sortable":true,"visible":true},{"key":"RATING_NOTE","label":"Status","type":"STRING","sortable":true,"visible":true}]~';
  upsert_report('travel-supplier-spend', 'Travel · Supplier Spend', 'Supplier spend and average cost per booking for the departure period.', 'store', q_id, f, c, 'TOTAL_COST', 'DESC', 70, v_report_id);
  grant_travel(v_report_id);

  IF v_travel_role_type IS NULL THEN
    DBMS_OUTPUT.PUT_LINE('[travel-reports] No Travel role with ROLE_TYPE — grant reports manually in Report Builder.');
  END IF;
END;
/

COMMIT;

PROMPT Travel reports seeded (codes: travel-revenue-profit, travel-bookings, …). Grant role: Travel.
