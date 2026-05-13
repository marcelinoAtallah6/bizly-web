package com.auth.api.controllers.dto.registration;

/**
 * Public sign-up payload — collected from the multi-step "Create Account" wizard.
 *
 * <p>Step 1 of the wizard captures the {@code username / email / password /
 * firstName / lastName / mobile} fields; step 2 captures {@code businessName /
 * businessType}. The wizard submits both steps together so the backend can run
 * a single atomic transaction (user → business → user_role).</p>
 *
 * <p>Security: the DTO intentionally has no {@code roleId} or {@code roleName}
 * field. The role assigned during registration is decided server-side
 * (the single role flagged {@code is_default_for_registration = 1}). Any
 * client-side attempt to inject roles is silently dropped by Jackson.</p>
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

	public String getBusinessType() { return businessType; }
	public void setBusinessType(String businessType) { this.businessType = businessType; }

	public String getAuthProvider() { return authProvider; }
	public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

	public String getProviderUserId() { return providerUserId; }
	public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
}
