-- =============================================================================
-- Per-user starred menus + which one is the post-login default.
--
-- Storage model:
--   * one row per (USERNAME, MENU_ROUTE) star.
--   * at most ONE row per user with IS_DEFAULT=1 — enforced in the SERVICE
--     LAYER (SettingsUserFavoritesService) with a three-step
--     dedupe → clear → set flow, and self-healed on every list().
--
-- Historical note:
--   Earlier versions of this patch created a function-based UNIQUE index
--   (UX_USER_FAV_DEFAULT) on (USERNAME, CASE WHEN IS_DEFAULT=1 THEN 'D' ELSE
--   NULL END). On Oracle 23 + Hibernate 5 that combo intermittently raised
--   ORA-00001 even for INSERTs of IS_DEFAULT=0 rows because of how the
--   driver bound boolean values during persistence-context flush. We drop
--   it here and replace it with a plain non-unique index for query speed.
--
-- We deliberately do NOT FK MENU_ROUTE -> UM_MENU.ROUTE so a temporarily
-- disabled menu doesn't cascade-delete the user's preference. The frontend
-- filters favorites against the live accessible-menu list at render time.
--
-- Idempotent: safe to re-run.
-- =============================================================================

SET DEFINE OFF;

-- 1) Table -------------------------------------------------------------------
DECLARE
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TABLES
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_USER_FAVORITE';

  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE UM.SETTINGS_USER_FAVORITE (
        USERNAME    VARCHAR2(120) NOT NULL,
        MENU_ROUTE  VARCHAR2(400) NOT NULL,
        PIN_ORDER   NUMBER(10) DEFAULT 0 NOT NULL,
        IS_DEFAULT  NUMBER(1)   DEFAULT 0 NOT NULL,
        CREATED_AT  TIMESTAMP   DEFAULT SYSTIMESTAMP NOT NULL,
        CONSTRAINT PK_SETTINGS_USER_FAV PRIMARY KEY (USERNAME, MENU_ROUTE),
        CONSTRAINT CK_SUF_DEFAULT CHECK (IS_DEFAULT IN (0,1))
      )
    ';
  END IF;
END;
/

-- 2) Query path: list favorites in pin order --------------------------------
DECLARE
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_INDEXES
   WHERE OWNER = 'UM' AND INDEX_NAME = 'IDX_USER_FAV_USER_ORDER';
  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE INDEX UM.IDX_USER_FAV_USER_ORDER
        ON UM.SETTINGS_USER_FAVORITE (USERNAME, PIN_ORDER)
    ';
  END IF;
END;
/

-- 3) Heal any existing duplicate IS_DEFAULT=1 rows BEFORE dropping the
--    unique index, so application reads stay deterministic on a stale DB.
DECLARE
  v_table NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_table FROM ALL_TABLES
   WHERE OWNER = 'UM' AND TABLE_NAME = 'SETTINGS_USER_FAVORITE';
  IF v_table > 0 THEN
    EXECUTE IMMEDIATE '
      UPDATE UM.SETTINGS_USER_FAVORITE
         SET IS_DEFAULT = 0
       WHERE IS_DEFAULT = 1
         AND ROWID NOT IN (
           SELECT MAX(ROWID) KEEP (DENSE_RANK LAST ORDER BY CREATED_AT)
             FROM UM.SETTINGS_USER_FAVORITE
            WHERE IS_DEFAULT = 1
            GROUP BY USERNAME
         )
    ';
  END IF;
END;
/

-- 4) Drop the problematic function-based unique index, if it exists.
DECLARE
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_INDEXES
   WHERE OWNER = 'UM' AND INDEX_NAME = 'UX_USER_FAV_DEFAULT';
  IF v_exists > 0 THEN
    EXECUTE IMMEDIATE 'DROP INDEX UM.UX_USER_FAV_DEFAULT';
  END IF;
END;
/

-- 5) Replace with a plain non-unique index for the common
--    "find this user's default(s)" lookup. No uniqueness enforced here —
--    that's the service layer's job (see SettingsUserFavoritesService).
DECLARE
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_INDEXES
   WHERE OWNER = 'UM' AND INDEX_NAME = 'IDX_USER_FAV_DEFAULT';
  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE INDEX UM.IDX_USER_FAV_DEFAULT
        ON UM.SETTINGS_USER_FAVORITE (USERNAME, IS_DEFAULT)
    ';
  END IF;
END;
/

COMMIT;
