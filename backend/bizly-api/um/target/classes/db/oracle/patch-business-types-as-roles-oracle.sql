-- =============================================================================
-- Business types-as-roles patch — UM schema
--
-- Replaces the hardcoded business-type catalog with rows in UM_ROLE flagged
-- IS_BUSINESS_TYPE = 1. After running this patch:
--   * The registration screen fetches the catalog from /auth/business-types
--     (no static UI list).
--   * Picking a business type at sign-up assigns the SAME role to the new
--     user — there is no separate BUSINESS_ADMIN any more.
--   * Adding a new business type is a single INSERT into UM_ROLE.
--
-- Idempotent. Safe to re-run on environments that have already been patched.
-- =============================================================================

-- 1) UM_ROLE — add IS_BUSINESS_TYPE column
DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM ALL_TAB_COLUMNS
   WHERE OWNER = 'UM' AND TABLE_NAME = 'UM_ROLE' AND COLUMN_NAME = 'IS_BUSINESS_TYPE';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE UM.UM_ROLE ADD (IS_BUSINESS_TYPE NUMBER(1) DEFAULT 0 NOT NULL)';
  END IF;
END;
/

-- 2) Retire BUSINESS_ADMIN — soft-disable it so existing user_role rows are not
--    orphaned. The role is removed from the registration picker (system-restricted)
--    and clears the legacy is_default_for_registration flag. Operators that want a
--    HARD delete should re-assign existing users first; see the commented block at
--    the bottom of this file.
DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM UM.UM_ROLE WHERE UPPER(NAME) = 'BUSINESS_ADMIN';
  IF v_count > 0 THEN
    UPDATE UM.UM_ROLE
       SET IS_DEFAULT_FOR_REGISTRATION = 0,
           IS_SYSTEM_RESTRICTED        = 1,
           IS_BUSINESS_TYPE            = 0
     WHERE UPPER(NAME) = 'BUSINESS_ADMIN';
  END IF;
END;
/

-- 3) Seed canonical business-type roles.
--
--    ID: many Oracle installs keep UM_ROLE.ID as a plain NUMBER (no identity).
--        MERGE inserts therefore must allocate the next id explicitly.
--
--    ROLE_TYPE: stable numeric codes used across the app (dashboard grants,
--        settings, etc.) — NOT the surrogate ID. Each business type gets its
--        own code in the 201–299 band to avoid colliding with legacy roles
--        (e.g. BUSINESS_ADMIN historically used 3). When adding a new type,
--        pick the next free integer in that band and document it here.
--
--    Canonical map (NAME → ROLE_TYPE):
--      RESTAURANT=201, CLINIC=202, BEAUTY_CENTER=203, GYM=204, TAXI_COMPANY=205,
--      RETAIL=206, SALON=207, SERVICES=208, OTHER=209
DECLARE
  v_business_lvl NUMBER;
BEGIN
  SELECT ID INTO v_business_lvl FROM UM.UM_ROLE_LEVEL WHERE CODE = 'BUSINESS';

  FOR rec IN (
    SELECT 'RESTAURANT'    AS NAME, 201 AS ROLE_TYPE FROM DUAL UNION ALL
    SELECT 'CLINIC'        AS NAME, 202 AS ROLE_TYPE FROM DUAL UNION ALL
    SELECT 'BEAUTY_CENTER' AS NAME, 203 AS ROLE_TYPE FROM DUAL UNION ALL
    SELECT 'GYM'           AS NAME, 204 AS ROLE_TYPE FROM DUAL UNION ALL
    SELECT 'TAXI_COMPANY'  AS NAME, 205 AS ROLE_TYPE FROM DUAL UNION ALL
    SELECT 'RETAIL'        AS NAME, 206 AS ROLE_TYPE FROM DUAL UNION ALL
    SELECT 'SALON'         AS NAME, 207 AS ROLE_TYPE FROM DUAL UNION ALL
    SELECT 'SERVICES'      AS NAME, 208 AS ROLE_TYPE FROM DUAL UNION ALL
    SELECT 'OTHER'         AS NAME, 209 AS ROLE_TYPE FROM DUAL
  ) LOOP
    MERGE INTO UM.UM_ROLE t
    USING (
      SELECT rec.NAME AS NAME, rec.ROLE_TYPE AS ROLE_TYPE FROM DUAL
    ) s
    ON (UPPER(TRIM(t.NAME)) = UPPER(TRIM(s.NAME)))
    WHEN MATCHED THEN UPDATE SET
      t.ROLE_LEVEL_ID               = v_business_lvl,
      t.ROLE_TYPE                   = s.ROLE_TYPE,
      t.IS_BUSINESS_TYPE            = 1,
      t.IS_SYSTEM_RESTRICTED        = 0,
      t.IS_DEFAULT_FOR_REGISTRATION = 0
    WHEN NOT MATCHED THEN INSERT (
      ID, NAME, ROLE_TYPE, ROLE_LEVEL_ID, IS_DEFAULT_FOR_REGISTRATION,
      IS_SYSTEM_RESTRICTED, IS_BUSINESS_TYPE, CREATED_AT
    ) VALUES (
      (SELECT NVL(MAX(x.ID), 0) + 1 FROM UM.UM_ROLE x),
      s.NAME,
      s.ROLE_TYPE,
      v_business_lvl,
      0,
      0,
      1,
      SYSTIMESTAMP
    );
  END LOOP;
END;
/

-- 4) Defence-in-depth: make sure SUPER_ADMIN / SYSTEM_* style rows are never marked
--    as a business type, even if a future schema change accidentally flips the flag.
UPDATE UM.UM_ROLE
   SET IS_BUSINESS_TYPE = 0
 WHERE IS_BUSINESS_TYPE = 1
   AND (UPPER(NAME) IN ('SUPER_ADMIN', 'SYSTEM_ADMIN', 'ADMIN')
        OR UPPER(NAME) LIKE 'SUPER%' OR UPPER(NAME) LIKE 'SYSTEM%'
        OR IS_SYSTEM_RESTRICTED = 1);

-- 5) Clear any leftover IS_DEFAULT_FOR_REGISTRATION flags. The new flow has no
--    "default" role — the user always picks one explicitly at sign-up time.
UPDATE UM.UM_ROLE SET IS_DEFAULT_FOR_REGISTRATION = 0
 WHERE IS_DEFAULT_FOR_REGISTRATION = 1;

COMMIT;

-- =============================================================================
-- OPTIONAL HARD DELETE OF BUSINESS_ADMIN
-- =============================================================================
-- Only run this if you have already re-assigned every user currently on
-- BUSINESS_ADMIN to one of the new business-type roles. The block below first
-- deletes UM_USER_ROLE rows pointing to BUSINESS_ADMIN (which would otherwise
-- block the role delete with a FK violation) and then deletes the role itself.
--
-- DECLARE
--   v_role_id NUMBER;
-- BEGIN
--   SELECT ID INTO v_role_id FROM UM.UM_ROLE WHERE UPPER(NAME) = 'BUSINESS_ADMIN';
--   DELETE FROM UM.UM_USER_ROLE WHERE ROLE_ID = v_role_id;
--   DELETE FROM UM.UM_ROLE WHERE ID = v_role_id;
--   COMMIT;
-- EXCEPTION
--   WHEN NO_DATA_FOUND THEN NULL;
-- END;
-- /

-- =============================================================================
-- VERIFICATION QUERIES
-- =============================================================================
-- SELECT NAME, ROLE_TYPE, ROLE_LEVEL_ID, IS_BUSINESS_TYPE, IS_SYSTEM_RESTRICTED, IS_DEFAULT_FOR_REGISTRATION
--   FROM UM.UM_ROLE
--  ORDER BY IS_BUSINESS_TYPE DESC, ROLE_TYPE, NAME;
--
-- SELECT NAME, ROLE_TYPE FROM UM.UM_ROLE WHERE IS_BUSINESS_TYPE = 1 AND IS_SYSTEM_RESTRICTED = 0 ORDER BY ROLE_TYPE;
