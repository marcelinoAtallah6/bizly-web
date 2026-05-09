package com.kyc.common;

public class DatabaseConstants {

    private DatabaseConstants() {
    }

    public static final String SCHEMA = "um";

    public static final String KYC_CUSTOMER_TABLE = "kyc_customer";
    public static final String KYC_CUSTOMER_DETAIL_TABLE = "kyc_customer_detail";
    public static final String USER_SESSION_TABLE = "um_user_session";

    public static final String KYC_CUSTOMER_SEQ = SCHEMA + ".KYC_CUSTOMER_SEQ";
    public static final String KYC_CUSTOMER_DETAIL_SEQ = SCHEMA + ".KYC_CUSTOMER_DETAIL_SEQ";
    public static final String USER_SESSION_SEQ = SCHEMA + ".USER_SESSION_SEQ";
}