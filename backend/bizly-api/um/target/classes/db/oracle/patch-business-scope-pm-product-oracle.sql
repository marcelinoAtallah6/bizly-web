-- =============================================================================
-- Worked example: scope PM_PRODUCT by business_id.
--
-- Pattern to copy when scoping any tenant-owned table:
--   1. ALTER TABLE … ADD (business_id NUMBER)
--   2. CREATE INDEX on (business_id, <natural-sort-column>)
--   3. Optionally add a FK to UM.UM_BUSINESS — only do so if a hard delete of
--      a business should never leave orphans. Most of the time the FK is
--      desirable; cross-schema FKs work in this codebase (other tables FK
--      UM.UM_USER).
--   4. Backfill: pick the first ACTIVE business and stamp it onto existing
--      rows so the running app keeps showing data until you migrate users.
-- =============================================================================

DECLARE
  v_count NUMBER;
  v_default_business NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM ALL_TAB_COLUMNS
   WHERE OWNER = 'UM' AND TABLE_NAME = 'PM_PRODUCT' AND COLUMN_NAME = 'BUSINESS_ID';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE UM.PM_PRODUCT ADD (BUSINESS_ID NUMBER)';
    EXECUTE IMMEDIATE 'CREATE INDEX UM.IDX_PM_PRODUCT_BUSINESS ON UM.PM_PRODUCT (BUSINESS_ID, NAME)';
    EXECUTE IMMEDIATE 'ALTER TABLE UM.PM_PRODUCT ADD CONSTRAINT FK_PM_PRODUCT_BUSINESS '
                    || 'FOREIGN KEY (BUSINESS_ID) REFERENCES UM.UM_BUSINESS (ID)';
  END IF;

  -- Backfill: only attempt if at least one business exists.
  SELECT COUNT(*) INTO v_count FROM UM.UM_BUSINESS;
  IF v_count > 0 THEN
    SELECT MIN(ID) INTO v_default_business FROM UM.UM_BUSINESS WHERE STATUS = 'ACTIVE';
    IF v_default_business IS NOT NULL THEN
      UPDATE UM.PM_PRODUCT SET BUSINESS_ID = v_default_business WHERE BUSINESS_ID IS NULL;
    END IF;
  END IF;
END;
/
COMMIT;
