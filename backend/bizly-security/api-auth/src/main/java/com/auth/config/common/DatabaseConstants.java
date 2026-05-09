package com.auth.config.common;

public class DatabaseConstants {

    private DatabaseConstants() {
    }

    public static final String SCHEMA = "um";

    public static final String USER_TABLE = "um_user";
    public static final String ROLE_TABLE = "um_role";
    public static final String USER_ROLE_TABLE = "um_user_role";
    public static final String USER_SESSION_TABLE = "um_user_session";

    public static final String USER_SEQ = SCHEMA + ".USER_SEQ";
    public static final String ROLE_SEQ = SCHEMA + ".ROLE_SEQ";
    public static final String USER_ROLE_SEQ = SCHEMA + ".USER_ROLE_SEQ";
    public static final String USER_SESSION_SEQ = SCHEMA + ".USER_SESSION_SEQ";
}