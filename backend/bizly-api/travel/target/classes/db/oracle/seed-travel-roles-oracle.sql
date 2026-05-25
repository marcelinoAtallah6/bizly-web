-- =============================================================================
-- Travel agency business-type roles (role_level = ADMIN for portal templates,
-- or BUSINESS team roles — tune ROLE_LEVEL_ID / ROLE_KIND to match your UM seed).
-- Assign PARENT_ROLE_ID to Super Admin when creating delegated travel ops roles.
-- =============================================================================

-- Example: insert ADMIN_INTERNAL travel roles when business registers as TRAVEL_AGENCY.
-- Role names must match JWT authority names (ROLE_<name>).

-- TRAVEL_OWNER — full agency access
-- TRAVEL_OPS_MANAGER
-- TRAVEL_SR_CONSULTANT
-- TRAVEL_CONSULTANT
-- TRAVEL_ACCOUNTS

-- After roles exist, grant menu permissions on routes:
--   /travel/clients, /travel/bookings, /travel/packages,
--   /travel/visas, /travel/documents, /travel/suppliers, /travel/finance,
--   /travel/commissions, /travel/follow-ups

COMMIT;
