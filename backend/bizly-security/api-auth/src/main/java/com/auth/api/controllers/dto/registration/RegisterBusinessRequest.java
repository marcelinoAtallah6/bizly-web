package com.auth.api.controllers.dto.registration;

/**
 * Payload for {@code POST /auth/register-business}.
 *
 * <p>By design this DTO carries <strong>no role id</strong>. The user can
 * never pick their own role — the server picks it from {@code um_role} based
 * on the role's level and {@code is_default_for_registration} flag.</p>
 *
 * <p>Note: even if a malicious client adds extra JSON fields like
 * {@code roleId}, Spring/Jackson silently ignores them because they're not
 * declared here.</p>
 */
public class RegisterBusinessRequest {

	private String businessName;
	private String businessType;

	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }

	public String getBusinessType() { return businessType; }
	public void setBusinessType(String businessType) { this.businessType = businessType; }
}
