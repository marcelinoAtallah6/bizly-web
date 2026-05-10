package com.notification.workflow;

public final class NotificationConstants {

	private NotificationConstants() {
	}

	public static final String TEMPLATE_WELCOME = "welcome_email";
	public static final String TEMPLATE_BIRTHDAY = "birthday_email";

	public static final String PROCESS_WELCOME = "WELCOME_EMAIL";
	public static final String PROCESS_BIRTHDAY = "BIRTHDAY_EMAIL";

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
