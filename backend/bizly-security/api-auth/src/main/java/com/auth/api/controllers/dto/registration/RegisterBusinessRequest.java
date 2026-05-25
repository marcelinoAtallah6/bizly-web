package com.auth.api.controllers.dto.registration;

/**
 * Payload for {@code POST /auth/register-business} — used by the post-login
 * "Set up your business" screen (typical after a social sign-up).
 *
 * <p>The caller picks a business-type role from {@code POST /auth/business-types}
 * and submits its id as {@code roleId}. The same id is assigned to the user
 * via {@code um_user_role} and cached on {@code um_business.business_type}.</p>
 *
 * <p>Security: the server only honours {@code roleId} values that resolve to a
 * BUSINESS-level, non-system, business-type role. SUPER_ADMIN and other
 * system roles are filtered out by both the listing endpoint and the resolver
 * — clients cannot promote themselves by guessing an id.</p>
 */
public class RegisterBusinessRequest {

	private String businessName;
	private Long roleId;
	/** Optional fallback — legacy clients sent a free-form code (e.g. {@code "RESTAURANT"}). */
	private String businessType;

	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }

	public Long getRoleId() { return roleId; }
	public void setRoleId(Long roleId) { this.roleId = roleId; }

	public String getBusinessType() { return businessType; }
	public void setBusinessType(String businessType) { this.businessType = businessType; }
}
