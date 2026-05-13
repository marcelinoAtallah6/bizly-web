-- =============================================================================
-- Migration: SETTINGS_DASH_ROLE_GRANT.ROLE_NAME (VARCHAR2) -> ROLE_TYPE (NUMBER)
--
-- Reason: dashboard role grants must survive UM_ROLE.NAME renames. UM_ROLE.ROLE_TYPE
--         is stable (e.g. 1 = ADMIN, 2 = USER, …) so we key on it instead.
--
-- Idempotent: skips work if ROLE_TYPE column already exists / ROLE_NAME already gone.
-- =============================================================================

SET DEFINE OFF;
SET SERVEROUTPUT ON;

DECLARE
  v_has_role_type NUMBER;
  v_has_role_name NUMBER;
  v_orphans       NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_has_role_type
    FROM ALL_TAB_COLS
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_DASH_ROLE_GRANT' AND COLUMN_NAME = 'ROLE_TYPE';

  SELECT COUNT(*) INTO v_has_role_name
    FROM ALL_TAB_COLS
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_DASH_ROLE_GRANT' AND COLUMN_NAME = 'ROLE_NAME';

  IF v_has_role_type = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE UM.SETTINGS_DASH_ROLE_GRANT ADD (ROLE_TYPE NUMBER(10))';
  END IF;

  IF v_has_role_name = 1 THEN
    EXECUTE IMMEDIATE
      'UPDATE UM.SETTINGS_DASH_ROLE_GRANT g
          SET g.ROLE_TYPE = (
            SELECT r.ROLE_TYPE FROM UM.UM_ROLE r
             WHERE UPPER(TRIM(r.NAME)) = UPPER(TRIM(g.ROLE_NAME))
               AND r.ROLE_TYPE IS NOT NULL
               AND ROWNUM = 1
          )
        WHERE g.ROLE_TYPE IS NULL';

    SELECT COUNT(*) INTO v_orphans
      FROM UM.SETTINGS_DASH_ROLE_GRANT WHERE ROLE_TYPE IS NULL;

    IF v_orphans > 0 THEN
      DBMS_OUTPUT.PUT_LINE('[patch-dash-grant-role-type] removing ' || v_orphans
                           || ' grant rows whose ROLE_NAME no longer matches any UM_ROLE.NAME / ROLE_TYPE');
      EXECUTE IMMEDIATE 'DELETE FROM UM.SETTINGS_DASH_ROLE_GRANT WHERE ROLE_TYPE IS NULL';
    END IF;
  END IF;

  EXECUTE IMMEDIATE 'ALTER TABLE UM.SETTINGS_DASH_ROLE_GRANT MODIFY (ROLE_TYPE NOT NULL)';

  BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE UM.SETTINGS_DASH_ROLE_GRANT DROP CONSTRAINT PK_SD_ROLE';
  EXCEPTION WHEN OTHERS THEN
    IF SQLCODE <> -2443 THEN  -- "constraint does not exist"
      RAISE;
    END IF;
  END;

  EXECUTE IMMEDIATE
    'ALTER TABLE UM.SETTINGS_DASH_ROLE_GRANT ADD CONSTRAINT PK_SD_ROLE PRIMARY KEY (DASHBOARD_ID, ROLE_TYPE)';

  IF v_has_role_name = 1 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE UM.SETTINGS_DASH_ROLE_GRANT DROP COLUMN ROLE_NAME';
  END IF;

  COMMIT;
END;
/
