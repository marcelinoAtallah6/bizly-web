package com.notification.workflow;

public final class NotificationConstants {

	private NotificationConstants() {
	}

	/** Preferred key for user welcome; falls back to {@link #TEMPLATE_WELCOME_LEGACY}. */
	public static final String TEMPLATE_WELCOME_USER = "WELCOME_USER";
	/** Legacy installs may still use this template key. */
	public static final String TEMPLATE_WELCOME_LEGACY = "welcome_email";

	public static final String TEMPLATE_WELCOME_CUSTOMER = "WELCOME_CUSTOMER";
	public static final String TEMPLATE_BIRTHDAY_USER = "BIRTHDAY_USER";
	public static final String TEMPLATE_BIRTHDAY_CUSTOMER = "BIRTHDAY_CUSTOMER";
	/** Legacy birthday template key. */
	public static final String TEMPLATE_BIRTHDAY_LEGACY = "birthday_email";

	public static final String TEMPLATE_BROADCAST_DEFAULT = "BROADCAST_DEFAULT";

	public static final String PROCESS_WELCOME = "WELCOME_EMAIL";
	public static final String PROCESS_WELCOME_CUSTOMER = "WELCOME_CUSTOMER_EMAIL";
	public static final String PROCESS_BIRTHDAY = "BIRTHDAY_EMAIL";
	public static final String PROCESS_BIRTHDAY_CUSTOMER = "BIRTHDAY_CUSTOMER_EMAIL";
	public static final String PROCESS_BROADCAST = "BROADCAST_EMAIL";

	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_FAILED = "FAILED";

	public static final String ACTIVE_YES = "Y";

	/** UM.UM_USER.NOTIF_WELCOME_FLAG: pending send / completed */
	public static final int WELCOME_FLAG_PENDING = 0;
	public static final int WELCOME_FLAG_SENT = 1;

	/** UM.UM_USER.NOTIF_WELCOME_STATUS: failure vs success of send */
	public static final int WELCOME_STATUS_NOT_OK = 0;
	public static final int WELCOME_STATUS_OK = 1;
}
