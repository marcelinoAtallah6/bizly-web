package com.auth.api.controllers.dto.registration;

import java.util.List;

/**
 * Response of {@code POST /auth/me}. Used by the frontend to decide the
 * post-login redirect target without parsing the JWT.
 */
public class MeResponse {

	private Long userId;
	private String username;
	private String email;
	private String firstName;
	private String lastName;

	/** Mirrors {@code um_user.mobile_number} for profile / shell forms. */
	private String mobileNumber;

	/** Present when the profile photo is small enough for the SPA shell; otherwise empty (use /auth/me/avatar). */
	private String profileImageMime;
	private String profileImageBase64;

	private Boolean firstLogin;
	private Long businessId;
	private String businessName;

	private List<String> roles;
	private String roleLevel;

	/** True when the current account can register a brand new business. */
	private Boolean canRegisterBusiness;

	/** Raw {@code um_business.status} when the user belongs to a tenant. */
	private String businessStatus;

	/** Convenience flag derived from {@link #businessStatus} for the SPA shell. */
	private Boolean pendingBusinessApproval;

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }

	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }

	public String getMobileNumber() { return mobileNumber; }
	public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

	public String getProfileImageMime() { return profileImageMime; }
	public void setProfileImageMime(String profileImageMime) { this.profileImageMime = profileImageMime; }

	public String getProfileImageBase64() { return profileImageBase64; }
	public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }

	public Boolean getFirstLogin() { return firstLogin; }
	public void setFirstLogin(Boolean firstLogin) { this.firstLogin = firstLogin; }

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }

	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }

	public List<String> getRoles() { return roles; }
	public void setRoles(List<String> roles) { this.roles = roles; }

	public String getRoleLevel() { return roleLevel; }
	public void setRoleLevel(String roleLevel) { this.roleLevel = roleLevel; }

	public Boolean getCanRegisterBusiness() { return canRegisterBusiness; }
	public void setCanRegisterBusiness(Boolean canRegisterBusiness) { this.canRegisterBusiness = canRegisterBusiness; }

	public String getBusinessStatus() { return businessStatus; }
	public void setBusinessStatus(String businessStatus) { this.businessStatus = businessStatus; }

	public Boolean getPendingBusinessApproval() { return pendingBusinessApproval; }
	public void setPendingBusinessApproval(Boolean pendingBusinessApproval) {
		this.pendingBusinessApproval = pendingBusinessApproval;
	}
}
