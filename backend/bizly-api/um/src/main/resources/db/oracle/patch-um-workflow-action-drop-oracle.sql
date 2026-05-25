-- =============================================================================
-- Drop legacy UM_WORKFLOW_ACTION (replaced by UM_WORKFLOW_API_ENDPOINT DOMAIN rows).
--
-- Prerequisites:
--   patch-um-workflow-endpoint-unified-catalog-oracle.sql (data migrated)
--   UM service stopped (optional; avoids JPA metadata cache on old entity)
-- =============================================================================

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE UM.UM_WORKFLOW_ACTION CASCADE CONSTRAINTS';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;  -- table does not exist
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE UM.S_UM_WORKFLOW_ACTION';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE NOT IN (-2289, -942) THEN RAISE; END IF;
END;
/

COMMIT;
