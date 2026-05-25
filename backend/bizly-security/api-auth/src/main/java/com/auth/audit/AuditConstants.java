package com.auth.audit;

public final class AuditConstants {

	private AuditConstants() {
	}

	public static final String AUDIT_LOG_TABLE = "UM_AUDIT_LOG";
	public static final String AUDIT_LOG_SEQ = "UM.S_UM_AUDIT_LOG";

	public static final String ACTION_LOGIN = "AUTH_LOGIN";
	public static final String ACTION_LOGOUT = "AUTH_LOGOUT";
	public static final String RESOURCE_AUTH = "AUTH";
}
