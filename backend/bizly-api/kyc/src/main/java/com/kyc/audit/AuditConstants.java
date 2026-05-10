package com.kyc.audit;

/** Same physical table as UM audit — shared Oracle schema {@code UM}. */
public final class AuditConstants {

	private AuditConstants() {
	}

	public static final String AUDIT_LOG_TABLE = "UM_AUDIT_LOG";
	public static final String AUDIT_LOG_SEQ = "UM.S_UM_AUDIT_LOG";
}
