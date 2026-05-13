-- =============================================================================
-- Per-user scalar preferences for the settings module.
-- Currently only stores LAST_DASHBOARD_ID (the dashboard the user opened last)
-- so the navbar can land them on it after login instead of always the first one.
--
-- Idempotent: no-op if the table already exists.
-- =============================================================================

SET DEFINE OFF;

DECLARE
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TABLES
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_USER_PREF';

  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE UM.SETTINGS_USER_PREF (
        USERNAME           VARCHAR2(120) NOT NULL,
        LAST_DASHBOARD_ID  NUMBER,
        UPDATED_AT         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
        CONSTRAINT PK_SETTINGS_USER_PREF PRIMARY KEY (USERNAME),
        CONSTRAINT FK_SUP_LAST_DASH FOREIGN KEY (LAST_DASHBOARD_ID)
          REFERENCES UM.SETTINGS_DASHBOARD(ID) ON DELETE SET NULL
      )
    ';
  END IF;
END;
/
