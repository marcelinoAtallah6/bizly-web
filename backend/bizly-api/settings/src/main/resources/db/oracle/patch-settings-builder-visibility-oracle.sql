-- =============================================================================
-- Builder visibility — role & user grants for SETTINGS_REPORT and SETTINGS_QUERY_DEF.
-- Schema: UM. Idempotent: safe to re-run.
--
-- Pattern mirrors SETTINGS_DASH_ROLE_GRANT / SETTINGS_DASH_USER_GRANT:
--   - role grants reference UM_ROLE.ROLE_TYPE (stable, survives renames)
--   - user grants reference UM_USER.USERNAME (case-insensitive lookup)
--   - no grants at all = "global" within the tenant (admins always bypass)
--
-- Each grant table is scoped per-tenant via business_id at read time using the
-- parent row's business_id (no separate business_id on the grant row to keep
-- the schema simple; admins can reassign by editing the parent).
-- =============================================================================

SET DEFINE OFF;
SET SERVEROUTPUT ON;

-- ----------------------------------------------------------------------------
-- SETTINGS_REPORT_ROLE_GRANT (report_id, role_type)
-- ----------------------------------------------------------------------------
DECLARE
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TABLES
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_REPORT_ROLE_GRANT';
  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE UM.SETTINGS_REPORT_ROLE_GRANT (
        REPORT_ID  NUMBER       NOT NULL,
        ROLE_TYPE  NUMBER(10)   NOT NULL,
        CREATED_AT TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
        CONSTRAINT PK_SR_ROLE PRIMARY KEY (REPORT_ID, ROLE_TYPE),
        CONSTRAINT FK_SR_ROLE_REPORT FOREIGN KEY (REPORT_ID)
          REFERENCES UM.SETTINGS_REPORT (ID) ON DELETE CASCADE
      )';
    EXECUTE IMMEDIATE 'CREATE INDEX IX_SR_ROLE_TYPE ON UM.SETTINGS_REPORT_ROLE_GRANT (ROLE_TYPE)';
    DBMS_OUTPUT.PUT_LINE('[visibility] SETTINGS_REPORT_ROLE_GRANT created');
  END IF;
END;
/

-- ----------------------------------------------------------------------------
-- SETTINGS_REPORT_USER_GRANT (report_id, username)
-- ----------------------------------------------------------------------------
DECLARE
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TABLES
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_REPORT_USER_GRANT';
  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE UM.SETTINGS_REPORT_USER_GRANT (
        REPORT_ID  NUMBER         NOT NULL,
        USERNAME   VARCHAR2(120)  NOT NULL,
        CREATED_AT TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL,
        CONSTRAINT PK_SR_USER PRIMARY KEY (REPORT_ID, USERNAME),
        CONSTRAINT FK_SR_USER_REPORT FOREIGN KEY (REPORT_ID)
          REFERENCES UM.SETTINGS_REPORT (ID) ON DELETE CASCADE
      )';
    EXECUTE IMMEDIATE 'CREATE INDEX IX_SR_USER_NAME ON UM.SETTINGS_REPORT_USER_GRANT (USERNAME)';
    DBMS_OUTPUT.PUT_LINE('[visibility] SETTINGS_REPORT_USER_GRANT created');
  END IF;
END;
/

-- ----------------------------------------------------------------------------
-- SETTINGS_QDEF_ROLE_GRANT (query_def_id, role_type)
-- ----------------------------------------------------------------------------
DECLARE
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TABLES
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_QDEF_ROLE_GRANT';
  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE UM.SETTINGS_QDEF_ROLE_GRANT (
        QUERY_DEF_ID NUMBER       NOT NULL,
        ROLE_TYPE    NUMBER(10)   NOT NULL,
        CREATED_AT   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
        CONSTRAINT PK_SQD_ROLE PRIMARY KEY (QUERY_DEF_ID, ROLE_TYPE),
        CONSTRAINT FK_SQD_ROLE_QDEF FOREIGN KEY (QUERY_DEF_ID)
          REFERENCES UM.SETTINGS_QUERY_DEF (ID) ON DELETE CASCADE
      )';
    EXECUTE IMMEDIATE 'CREATE INDEX IX_SQD_ROLE_TYPE ON UM.SETTINGS_QDEF_ROLE_GRANT (ROLE_TYPE)';
    DBMS_OUTPUT.PUT_LINE('[visibility] SETTINGS_QDEF_ROLE_GRANT created');
  END IF;
END;
/

-- ----------------------------------------------------------------------------
-- SETTINGS_QDEF_USER_GRANT (query_def_id, username)
-- ----------------------------------------------------------------------------
DECLARE
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TABLES
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_QDEF_USER_GRANT';
  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE UM.SETTINGS_QDEF_USER_GRANT (
        QUERY_DEF_ID NUMBER         NOT NULL,
        USERNAME     VARCHAR2(120)  NOT NULL,
        CREATED_AT   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL,
        CONSTRAINT PK_SQD_USER PRIMARY KEY (QUERY_DEF_ID, USERNAME),
        CONSTRAINT FK_SQD_USER_QDEF FOREIGN KEY (QUERY_DEF_ID)
          REFERENCES UM.SETTINGS_QUERY_DEF (ID) ON DELETE CASCADE
      )';
    EXECUTE IMMEDIATE 'CREATE INDEX IX_SQD_USER_NAME ON UM.SETTINGS_QDEF_USER_GRANT (USERNAME)';
    DBMS_OUTPUT.PUT_LINE('[visibility] SETTINGS_QDEF_USER_GRANT created');
  END IF;
END;
/

COMMIT;
