package com.pm.common;

public class DatabaseConstants {

    private DatabaseConstants() {
    }

    public static final String SCHEMA = "um";

    public static final String PRODUCT_TABLE = "pm_product";
    public static final String PRODUCT_ITEMS_TABLE = "pm_product_items";
    public static final String KYC_CUSTOMER_ORDER_TABLE = "kyc_customer_order";
    public static final String CUSTOMER_SALE_TABLE = "pm_customer_sale";
    public static final String CUSTOMER_SALE_LINE_TABLE = "pm_customer_sale_line";
    /**
     * Read-only mapping owned by BM. PM joins it during mixed product+service checkout so the unified
     * {@code /pm/sale/checkout} endpoint can validate and price BM service lines without a round-trip
     * to the BM service. BM remains the source of truth for service item lifecycle.
     */
    public static final String BM_SERVICE_ITEM_TABLE = "bm_service_item";
    public static final String USER_SESSION_TABLE = "um_user_session";

    public static final String PRODUCT_SEQ = SCHEMA + ".PM_PRODUCT_SEQ";
    public static final String PRODUCT_ITEM_SEQ = SCHEMA + ".PM_PRODUCT_ITEMS_SEQ";
    public static final String KYC_CUSTOMER_ORDER_SEQ = SCHEMA + ".KYC_CUSTOMER_ORDER_SEQ";
    public static final String USER_SESSION_SEQ = SCHEMA + ".USER_SESSION_SEQ";
}