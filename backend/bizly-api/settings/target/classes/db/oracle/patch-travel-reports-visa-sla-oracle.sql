-- Fix ORA-00932 on Travel Visa SLA report (TIMESTAMP - TIMESTAMP → INTERVAL, not NUMBER).
-- Run as UM user after seed-travel-reports-oracle.sql if that report already exists.

SET DEFINE OFF;

UPDATE um.settings_query_def
   SET sql_text = q'~
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
~',
       updated_at = SYSTIMESTAMP
 WHERE UPPER(TRIM(name)) = 'TRAVEL_Q_VISA_SLA';

COMMIT;

PROMPT Patched TRAVEL_Q_VISA_SLA query (Visa SLA report).
