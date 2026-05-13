-- =============================================================================
-- SETTINGS_DASH_ROLE_GRANT: ensure ROLE_NAME exists and is nullable so JPA can
-- populate it for legacy NOT NULL installs while ROLE_TYPE remains canonical.
-- Idempotent — safe to re-run.
-- =============================================================================
DECLARE
  v_cols NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_cols FROM ALL_TAB_COLUMNS
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_DASH_ROLE_GRANT' AND COLUMN_NAME = 'ROLE_NAME';

  IF v_cols = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE UM.SETTINGS_DASH_ROLE_GRANT ADD (ROLE_NAME VARCHAR2(200))';
  ELSE
    BEGIN
      EXECUTE IMMEDIATE 'ALTER TABLE UM.SETTINGS_DASH_ROLE_GRANT MODIFY (ROLE_NAME NULL)';
    EXCEPTION WHEN OTHERS THEN NULL;
    END;
  END IF;
END;
/

-- Backfill denormalized names from UM_ROLE.ROLE_TYPE (not UM_ROLE.ID)
BEGIN
  EXECUTE IMMEDIATE
    'UPDATE UM.SETTINGS_DASH_ROLE_GRANT g
        SET ROLE_NAME = (
              SELECT MIN(R.NAME) FROM UM.UM_ROLE R WHERE R.ROLE_TYPE = g.ROLE_TYPE
            )
      WHERE g.ROLE_NAME IS NULL
        AND EXISTS (SELECT 1 FROM UM.UM_ROLE R2 WHERE R2.ROLE_TYPE = g.ROLE_TYPE)';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

COMMIT;
