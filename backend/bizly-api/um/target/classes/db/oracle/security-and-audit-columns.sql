-- Run as UM schema owner (ddl-auto is none). Adjust names if your catalog differs.

-- Login lockout columns on um_user
ALTER TABLE um.um_user ADD failed_login_attempts NUMBER(3) DEFAULT 0;
ALTER TABLE um.um_user ADD account_locked NUMBER(1) DEFAULT 0;

-- Session active role (narrow permissions when user switches role in UI)
ALTER TABLE um.um_user_session ADD active_role_name VARCHAR2(64);

-- Menu visibility: comma-separated roles (empty = all roles). Examples: USER,ADMIN or ROLE_USER,ROLE_ADMIN
ALTER TABLE um.um_applications ADD allowed_roles VARCHAR2(512);
ALTER TABLE um.um_menus ADD allowed_roles VARCHAR2(512);

-- Audit trail
CREATE SEQUENCE um.s_um_audit_log START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE um.um_audit_log (
  id NUMBER(19) NOT NULL PRIMARY KEY,
  username VARCHAR2(128),
  action_code VARCHAR2(128) NOT NULL,
  resource_type VARCHAR2(128),
  resource_id VARCHAR2(128),
  old_values CLOB,
  new_values CLOB,
  http_method VARCHAR2(16),
  request_path VARCHAR2(512),
  ip_address VARCHAR2(64),
  session_id VARCHAR2(128),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE um.um_audit_log IS 'Append-only audit entries from @Audited controllers';

create or replace TRIGGER um.um_audit_log_id
BEFORE INSERT ON um.um_audit_log
FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT um.s_um_audit_log.NEXTVAL INTO :NEW.id FROM dual;
    END IF;
END;
