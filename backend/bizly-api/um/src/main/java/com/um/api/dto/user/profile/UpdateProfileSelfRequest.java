package com.um.api.dto.user.profile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Self-service profile update. Email / auth provider / provider user id are never accepted here —
 * they are ignored if sent; only {@link com.um.api.service.user.UserServiceImpl#updateSelfProfile}
 * mutates whitelisted columns.
 */
public class UpdateProfileSelfRequest {

	@NotBlank
	@Size(max = 64)
	private String firstName;

	@NotBlank
	@Size(max = 64)
	private String lastName;

	/** Optional; stored as {@code um_user.mobile_number}. */
	@Size(max = 40)
	private String phoneNumber;

	private String profileImageMimeType;
	private String profileImageBase64;
	private Boolean clearProfileImage;

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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getProfileImageMimeType() {
		return profileImageMimeType;
	}

	public void setProfileImageMimeType(String profileImageMimeType) {
		this.profileImageMimeType = profileImageMimeType;
	}

	public String getProfileImageBase64() {
		return profileImageBase64;
	}

	public void setProfileImageBase64(String profileImageBase64) {
		this.profileImageBase64 = profileImageBase64;
	}

	public Boolean getClearProfileImage() {
		return clearProfileImage;
	}

	public void setClearProfileImage(Boolean clearProfileImage) {
		this.clearProfileImage = clearProfileImage;
	}
}
