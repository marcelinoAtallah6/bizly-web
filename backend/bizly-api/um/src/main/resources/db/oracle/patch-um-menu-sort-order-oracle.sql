-- =============================================================================
-- Configurable ordering for sidebar applications and menus.
-- Run as UM schema owner, then restart the um service.
-- =============================================================================

BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE UM.UM_APPLICATIONS ADD SORT_ORDER NUMBER(10) DEFAULT 0 NOT NULL';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1430 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE UM.UM_MENUS ADD SORT_ORDER NUMBER(10) DEFAULT 0 NOT NULL';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1430 THEN RAISE; END IF;
END;
/

-- Backfill: preserve current implicit order (by id) until an admin reorders in Application Builder.
UPDATE UM.UM_APPLICATIONS SET SORT_ORDER = ID WHERE SORT_ORDER IS NULL OR SORT_ORDER = 0;
UPDATE UM.UM_MENUS SET SORT_ORDER = ID WHERE SORT_ORDER IS NULL OR SORT_ORDER = 0;

COMMIT;
