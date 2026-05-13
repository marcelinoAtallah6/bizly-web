package com.broadcast.common;

public class DatabaseConstants {

    private DatabaseConstants() {
    }

    public static final String SCHEMA = "um";

    public static final String PRODUCT_TABLE = "pm_product";
    public static final String PRODUCT_ITEMS_TABLE = "pm_product_items";
    public static final String KYC_CUSTOMER_ORDER_TABLE = "kyc_customer_order";
    public static final String CUSTOMER_SALE_TABLE = "pm_customer_sale";
    public static final String CUSTOMER_SALE_LINE_TABLE = "pm_customer_sale_line";
    public static final String BM_SERVICE_ITEM_TABLE = "bm_service_item";
    public static final String BM_APPOINTMENT_TABLE = "bm_appointment";
    public static final String KYC_CUSTOMER_TABLE = "kyc_customer";
    public static final String USER_SESSION_TABLE = "um_user_session";

    public static final String NOTIF_BROADCAST_MESSAGE_TABLE = "NOTIF_BROADCAST_MESSAGE";
    public static final String NOTIF_BROADCAST_MESSAGE_SEQ = "UM.NOTIF_BROADCAST_MESSAGE_SEQ";
    public static final String NOTIF_EMAIL_TEMPLATE_TABLE = "NOTIF_EMAIL_TEMPLATE";

    public static final String PRODUCT_SEQ = SCHEMA + ".PM_PRODUCT_SEQ";
    public static final String PRODUCT_ITEM_SEQ = SCHEMA + ".PM_PRODUCT_ITEMS_SEQ";
    public static final String KYC_CUSTOMER_ORDER_SEQ = SCHEMA + ".KYC_CUSTOMER_ORDER_SEQ";
    public static final String USER_SESSION_SEQ = SCHEMA + ".USER_SESSION_SEQ";
}