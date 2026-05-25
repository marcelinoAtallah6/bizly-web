package com.auth.api.controllers.dto.registration;

/**
 * Public sign-up payload — collected from the multi-step "Create Account" wizard.
 *
 * <p>Step 1 of the wizard captures the {@code username / email / password /
 * firstName / lastName / mobile} fields; step 2 captures {@code businessName}
 * + the chosen {@code roleId} (= business type). The wizard submits both
 * steps together so the backend can run a single atomic transaction
 * (user → business → user_role).</p>
 *
 * <p>Security model: the business-type list returned by
 * {@code POST /auth/business-types} only includes BUSINESS-level, non-system
 * roles flagged {@code is_business_type = 1}. The server re-validates the
 * {@code roleId} sent here against the same predicates before assigning it —
 * a client cannot promote itself to SUPER_ADMIN by tampering with the
 * payload even if it knows the id.</p>
 */
public class RegisterRequest {

	private String username;
	private String email;
	private String password;
	private String confirmPassword;
	private String firstName;
	private String lastName;
	private String mobileNumber;

	private String businessName;

	/**
	 * The chosen business-type role id (= row in {@code um_role}). Required.
	 * The same id is assigned to the new user via {@code um_user_role} and
	 * cached as {@code um_business.business_type = role.name} for reporting.
	 */
	private Long roleId;

	/**
	 * Legacy / optional. Older clients sent a free-form code (e.g.
	 * {@code "RESTAURANT"}) here. The server now treats it as a fallback when
	 * {@code roleId} is missing: it is resolved against {@code um_role.name}
	 * and must match a BUSINESS-level row flagged {@code is_business_type=1}.
	 * Tampered values that don't match such a row are rejected.
	 */
	private String businessType;

	/**
	 * Optional. When the user clicked a social-provider button on the sign-up
	 * page we pass the provider name + verified provider user id here so the
	 * service can store them on the new user record without a separate API
	 * round-trip. Manual sign-ups leave these null.
	 */
	private String authProvider;
	private String providerUserId;

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getConfirmPassword() { return confirmPassword; }
	public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }

	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }

	public String getMobileNumber() { return mobileNumber; }
	public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }

	public Long getRoleId() { return roleId; }
	public void setRoleId(Long roleId) { this.roleId = roleId; }

	public String getBusinessType() { return businessType; }
	public void setBusinessType(String businessType) { this.businessType = businessType; }

	public String getAuthProvider() { return authProvider; }
	public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

	public String getProviderUserId() { return providerUserId; }
	public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
}
