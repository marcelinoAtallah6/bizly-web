package com.um.api.model.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.um.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.USER_TABLE, schema = DatabaseConstants.SCHEMA)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	@SequenceGenerator(name = "user_seq", sequenceName = DatabaseConstants.USER_SEQ, allocationSize = 1)
	private Long id;

	@Column(name = "username", nullable = false, unique = true)
	private String username;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name = "last_name", nullable = false)
	private String lastName;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "mobile_number", nullable = false)
	private String mobileNumber;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "date_of_birth")
	private LocalDate dateOfBirth;

	/** 0 = welcome workflow pending; 1 = welcome email completed successfully. */
	@Column(name = "notif_welcome_flag")
	private Integer notifWelcomeFlag = 0;

	/** 0 = not successful (never sent or last attempt failed); 1 = last send succeeded. */
	@Column(name = "notif_welcome_status")
	private Integer notifWelcomeStatus = 0;

	@Column(name = "profile_image_mime", length = 64)
	private String profileImageMime;

	@Lob
	@Column(name = "profile_image_data")
	private byte[] profileImageData;

	/** Tenant scope. NULL only for system-wide admin accounts. */
	@Column(name = "business_id")
	private Long businessId;

	/** 1 = user has not yet finished the welcome wizard. */
	@Column(name = "first_login")
	private Integer firstLogin;

	@Column(name = "welcome_completed_at")
	private java.time.LocalDateTime welcomeCompletedAt;

	@Column(name = "auth_provider", length = 20)
	private String authProvider;

	@Column(name = "provider_user_id", length = 200)
	private String providerUserId;

	@Column(name = "user_type", length = 30)
	private String userType = "BUSINESS_OWNER";

	@Column(name = "registration_source", length = 30)
	private String registrationSource = "PUBLIC";

	@Column(name = "email_verified_at")
	private LocalDateTime emailVerifiedAt;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	@Column(name = "is_business_owner")
	private Integer isBusinessOwner = 0;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	public boolean isBusinessOwnerFlag() {
		return isBusinessOwner != null && isBusinessOwner == 1;
	}

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }

	public Integer getFirstLogin() { return firstLogin; }
	public void setFirstLogin(Integer firstLogin) { this.firstLogin = firstLogin; }

	public java.time.LocalDateTime getWelcomeCompletedAt() { return welcomeCompletedAt; }
	public void setWelcomeCompletedAt(java.time.LocalDateTime welcomeCompletedAt) { this.welcomeCompletedAt = welcomeCompletedAt; }

	public String getAuthProvider() { return authProvider; }
	public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

	public String getProviderUserId() { return providerUserId; }
	public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public Integer getNotifWelcomeFlag() {
		return notifWelcomeFlag;
	}

	public void setNotifWelcomeFlag(Integer notifWelcomeFlag) {
		this.notifWelcomeFlag = notifWelcomeFlag;
	}

	public Integer getNotifWelcomeStatus() {
		return notifWelcomeStatus;
	}

	public void setNotifWelcomeStatus(Integer notifWelcomeStatus) {
		this.notifWelcomeStatus = notifWelcomeStatus;
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

	public String getUserType() { return userType; }
	public void setUserType(String userType) { this.userType = userType; }
	public String getRegistrationSource() { return registrationSource; }
	public void setRegistrationSource(String registrationSource) { this.registrationSource = registrationSource; }
	public LocalDateTime getEmailVerifiedAt() { return emailVerifiedAt; }
	public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
	public LocalDateTime getExpiresAt() { return expiresAt; }
	public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
	public Integer getIsBusinessOwner() { return isBusinessOwner; }
	public void setIsBusinessOwner(Integer isBusinessOwner) { this.isBusinessOwner = isBusinessOwner; }
	public LocalDateTime getLastLoginAt() { return lastLoginAt; }
	public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
