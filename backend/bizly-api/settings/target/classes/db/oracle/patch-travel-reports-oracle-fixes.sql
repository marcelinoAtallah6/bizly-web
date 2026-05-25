-- Fix ORA-00979 (GROUP BY with bind) and ORA-00932 (DATE vs NUMBER on date filters)
-- for travel report queries. Run as UM user on databases already seeded.

SET DEFINE OFF;

UPDATE um.settings_query_def
   SET sql_text = q'~
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
~',
       updated_at = SYSTIMESTAMP
 WHERE UPPER(TRIM(name)) = 'TRAVEL_Q_REVENUE_PROFIT';

UPDATE um.settings_query_def
   SET sql_text = q'~
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
~',
       updated_at = SYSTIMESTAMP
 WHERE UPPER(TRIM(name)) = 'TRAVEL_Q_BOOKINGS_DETAIL';

UPDATE um.settings_query_def
   SET sql_text = q'~
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
~',
       updated_at = SYSTIMESTAMP
 WHERE UPPER(TRIM(name)) = 'TRAVEL_Q_BOOKINGS_SUMMARY';

UPDATE um.settings_query_def
   SET sql_text = q'~
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
~',
       updated_at = SYSTIMESTAMP
 WHERE UPPER(TRIM(name)) = 'TRAVEL_Q_CONSULTANT_PERF';

UPDATE um.settings_query_def
   SET sql_text = q'~
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
~',
       updated_at = SYSTIMESTAMP
 WHERE UPPER(TRIM(name)) = 'TRAVEL_Q_SUPPLIER_SPEND';

COMMIT;

PROMPT Patched travel report queries (revenue GROUP BY, date filter casts).
