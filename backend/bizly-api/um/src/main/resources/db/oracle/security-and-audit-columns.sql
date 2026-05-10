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

-- Role × menu permissions (View / Add / Edit / Delete). If a role has at least one row, navigation uses strict filtering.
CREATE TABLE UM.UM_ROLE_MENU_PERM (
  role_id NUMBER(19) NOT NULL,
  menu_id NUMBER(19) NOT NULL,
  allow_view NUMBER(1) DEFAULT 0 NOT NULL,
  allow_add NUMBER(1) DEFAULT 0 NOT NULL,
  allow_edit NUMBER(1) DEFAULT 0 NOT NULL,
  allow_delete NUMBER(1) DEFAULT 0 NOT NULL,
  CONSTRAINT pk_um_role_menu_perm PRIMARY KEY (role_id, menu_id)
);

-- Existing installs: add Add permission column (ignore ORA-01430 if already applied).
-- ALTER TABLE UM.UM_ROLE_MENU_PERM ADD allow_add NUMBER(1);
-- UPDATE UM.UM_ROLE_MENU_PERM SET allow_add = 0 WHERE allow_add IS NULL;
-- ALTER TABLE UM.UM_ROLE_MENU_PERM MODIFY allow_add NUMBER(1) DEFAULT 0 NOT NULL;

ALTER TABLE UM.UM_ROLE_MENU_PERM ADD CONSTRAINT fk_um_rmp_role FOREIGN KEY (role_id) REFERENCES UM.um_role(id);
ALTER TABLE UM.UM_ROLE_MENU_PERM ADD CONSTRAINT fk_um_rmp_menu FOREIGN KEY (menu_id) REFERENCES UM.UM_MENUS(id);

COMMENT ON TABLE UM.UM_ROLE_MENU_PERM IS 'Per-role menu access; drives sidebar when configured';

create or replace TRIGGER um.um_audit_log_id
BEFORE INSERT ON um.um_audit_log
FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        SELECT um.s_um_audit_log.NEXTVAL INTO :NEW.id FROM dual;
    END IF;
END;
