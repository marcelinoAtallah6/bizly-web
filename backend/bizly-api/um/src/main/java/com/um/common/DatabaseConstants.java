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

    // Sequences
    public static final String USER_SEQ = SCHEMA + ".USER_SEQ";
    public static final String ROLE_SEQ = SCHEMA + ".ROLE_SEQ";
    public static final String APPLICATIONS_SEQ = SCHEMA + ".UM_APPLICATIONS_SEQ";
    public static final String MENUS_SEQ = SCHEMA + ".UM_MENUS_SEQ";
}