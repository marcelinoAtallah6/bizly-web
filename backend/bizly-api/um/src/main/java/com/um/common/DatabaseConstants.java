package com.um.common;

public class DatabaseConstants {

    private DatabaseConstants() {
    }

    // Schema
    public static final String SCHEMA = "UM";

    // Tables
    public static final String USER_TABLE = "um_user";
    public static final String ROLE_TABLE = "um_role";
    public static final String USER_ROLE_TABLE = "um_user_role";
    public static final String APPLICATIONS_TABLE = "UM_APPLICATIONS";
    public static final String MENUS_TABLE = "UM_MENUS";
    public static final String AUDIT_LOG_TABLE = "UM_AUDIT_LOG";
    public static final String WORKFLOW_CONFIG_TABLE = "UM_WORKFLOW_CONFIG";
    public static final String WORKFLOW_INSTANCE_TABLE = "UM_WORKFLOW_INSTANCE";
    public static final String WORKFLOW_API_ENDPOINT_TABLE = "UM_WORKFLOW_API_ENDPOINT";
    public static final String WORKFLOW_DEFINITION_TABLE = "UM_WORKFLOW_DEFINITION";
    public static final String WORKFLOW_STEP_TABLE = "UM_WORKFLOW_STEP";
    public static final String NOTIF_OUTBOX_TABLE = "UM_NOTIF_OUTBOX";
    public static final String WORKFLOW_TRIGGER_QUEUE_TABLE = "UM_WORKFLOW_TRIGGER_QUEUE";

    // Sequences
    public static final String USER_SEQ = SCHEMA + ".USER_SEQ";
    public static final String ROLE_SEQ = SCHEMA + ".ROLE_SEQ";
    public static final String APPLICATIONS_SEQ = SCHEMA + ".UM_APPLICATIONS_SEQ";
    public static final String MENUS_SEQ = SCHEMA + ".UM_MENUS_SEQ";
    public static final String AUDIT_LOG_SEQ = SCHEMA + ".S_UM_AUDIT_LOG";
    public static final String WORKFLOW_CONFIG_SEQ = SCHEMA + ".S_UM_WORKFLOW_CONFIG";
    public static final String WORKFLOW_INSTANCE_SEQ = SCHEMA + ".S_UM_WORKFLOW_INSTANCE";
    public static final String WORKFLOW_API_ENDPOINT_SEQ = SCHEMA + ".S_UM_WORKFLOW_API_ENDPOINT";
    public static final String WORKFLOW_DEFINITION_SEQ = SCHEMA + ".S_UM_WORKFLOW_DEFINITION";
    public static final String WORKFLOW_STEP_SEQ = SCHEMA + ".S_UM_WORKFLOW_STEP";
    public static final String NOTIF_OUTBOX_SEQ = SCHEMA + ".S_UM_NOTIF_OUTBOX";
    public static final String WORKFLOW_TRIGGER_QUEUE_SEQ = SCHEMA + ".S_UM_WORKFLOW_TRIGGER_QUEUE";
}