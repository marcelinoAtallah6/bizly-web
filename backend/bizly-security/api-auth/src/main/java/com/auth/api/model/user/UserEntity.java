package com.auth.api.model.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "um_user", schema = "um")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	@SequenceGenerator(name = "user_seq", sequenceName = "UM.USER_SEQ", allocationSize = 1)
	private long id;

	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = true)
	private String email;

	@Column(name = "profile_image_mime", length = 64)
	private String profileImageMime;

	@Lob
	@Column(name = "profile_image_data")
	private byte[] profileImageData;

	@Column(name = "failed_login_attempts")
	private int failedLoginAttempts;

	@Column(name = "account_locked")
	private boolean accountLocked;

	/** Tenant scoping. NULL for system-wide admin accounts that span every business. */
	@Column(name = "business_id")
	private Long businessId;

	/**
	 * 1 until the user finishes the welcome wizard. Flipped to 0 by
	 * {@code /auth/welcome-complete}.
	 */
	@Column(name = "first_login", nullable = false)
	private Integer firstLogin;

	@Column(name = "welcome_completed_at")
	private java.time.LocalDateTime welcomeCompletedAt;

	@Column(name = "auth_provider", nullable = false, length = 20)
	private String authProvider;

	@Column(name = "provider_user_id", length = 200)
	private String providerUserId;

	@Column(name = "mobile_number", length = 40)
	private String mobileNumber;

	/** Required by UM; Auth registration must set this (defaults to {@code ACTIVE}). */
	@Column(name = "status", nullable = false, length = 50)
	private String status;

	@Column(name = "user_type", length = 30)
	private String userType = "BUSINESS_OWNER";

	@Column(name = "registration_source", length = 30)
	private String registrationSource = "PUBLIC";

	@Column(name = "email_verified_at")
	private java.time.LocalDateTime emailVerifiedAt;

	@Column(name = "expires_at")
	private java.time.LocalDateTime expiresAt;

	@Column(name = "is_business_owner")
	private Integer isBusinessOwner = 0;

	@Column(name = "last_login_at")
	private java.time.LocalDateTime lastLoginAt;

	public boolean isBusinessOwnerFlag() {
		return isBusinessOwner != null && isBusinessOwner == 1;
	}

	public UserEntity() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getProfileImageMime() {
		return profileImageMime;
	}

	public void setProfileImageMime(String profileImageMime) {
		this.profileImageMime = profileImageMime;
	}

	public byte[] getProfileImageData() {
		return profileImageData;
	}

	public void setProfileImageData(byte[] profileImageData) {
		this.profileImageData = profileImageData;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public boolean isAccountLocked() {
		return accountLocked;
	}

	public void setAccountLocked(boolean accountLocked) {
		this.accountLocked = accountLocked;
	}

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }

	public Integer getFirstLogin() { return firstLogin; }
	public void setFirstLogin(Integer firstLogin) { this.firstLogin = firstLogin; }
	public boolean isFirstLogin() { return firstLogin != null && firstLogin == 1; }

	public java.time.LocalDateTime getWelcomeCompletedAt() { return welcomeCompletedAt; }
	public void setWelcomeCompletedAt(java.time.LocalDateTime welcomeCompletedAt) { this.welcomeCompletedAt = welcomeCompletedAt; }

	public String getAuthProvider() { return authProvider; }
	public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

	public String getProviderUserId() { return providerUserId; }
	public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUserType() { return userType; }
	public void setUserType(String userType) { this.userType = userType; }
	public String getRegistrationSource() { return registrationSource; }
	public void setRegistrationSource(String registrationSource) { this.registrationSource = registrationSource; }
	public java.time.LocalDateTime getEmailVerifiedAt() { return emailVerifiedAt; }
	public void setEmailVerifiedAt(java.time.LocalDateTime emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
	public java.time.LocalDateTime getExpiresAt() { return expiresAt; }
	public void setExpiresAt(java.time.LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
	public Integer getIsBusinessOwner() { return isBusinessOwner; }
	public void setIsBusinessOwner(Integer isBusinessOwner) { this.isBusinessOwner = isBusinessOwner; }
	public java.time.LocalDateTime getLastLoginAt() { return lastLoginAt; }
	public void setLastLoginAt(java.time.LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
