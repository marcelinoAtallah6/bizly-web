-- =============================================================================
-- Patch: remove every static / leftover "Reports" entry from UM_MENUS.
--
-- Reports created in the Report Builder are NOT shown in the sidebar — they
-- are only listed under "Saved reports" inside the Report Builder admin
-- screen. This patch cleans up any rows that were ever auto-added there:
--   1) the legacy placeholder route /rpt
--   2) any orphan row literally named "Reports"
--   3) any /reporting/run/<id> rows from a prior auto-sync attempt
--
-- Idempotent: safe to re-run.
-- =============================================================================

DELETE FROM UM.UM_MENUS
 WHERE ROUTE = '/rpt';

DELETE FROM UM.UM_MENUS
 WHERE NAME = 'Reports' AND (ROUTE IS NULL OR ROUTE = '');

DELETE FROM UM.UM_MENUS
 WHERE ROUTE LIKE '/reporting/run/%';

COMMIT;

-- Verify:
--   SELECT ID, NAME, ROUTE FROM UM.UM_MENUS
--    WHERE NAME = 'Reports' OR ROUTE = '/rpt' OR ROUTE LIKE '/reporting/run/%';
-- (should return zero rows)
