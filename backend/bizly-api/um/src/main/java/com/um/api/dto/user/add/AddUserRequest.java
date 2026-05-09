package com.um.api.dto.user.add;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.util.List;

import com.um.common.ApiDefaultValdiation;

public class AddUserRequest {

	@NotBlank(message = ApiDefaultValdiation.USERNAME)
	@Size(max = 50)
	private String username;

	@NotBlank(message = ApiDefaultValdiation.FIRST_NAME)
	@Size(max = 100)
	private String firstName;

	@NotBlank(message = ApiDefaultValdiation.LAST_NAME)
	@Size(max = 100)
	private String lastName;

	@NotBlank(message = ApiDefaultValdiation.EMAIL)
	@Email(message = ApiDefaultValdiation.EMAIL)
	private String email;

	@NotBlank(message = ApiDefaultValdiation.MOBILE_NUMBER)
	@Pattern(regexp = ApiDefaultValdiation.REGEX_MOBILE_NUMBER, message = ApiDefaultValdiation.MOBILE_NUMBER)
	private String mobileNumber;

	@NotBlank(message = ApiDefaultValdiation.PASSWORD)
	private String password;

	@NotEmpty(message = ApiDefaultValdiation.ROLES)
	private List<Long> roleIds;

	@NotBlank(message = ApiDefaultValdiation.STATUS)
	@Size(max = 50)
	private String status;

	/** Optional: e.g. image/jpeg — required if profileImageBase64 is set */
	private String profileImageMimeType;

	/** Optional raw Base64 or data URL */
	private String profileImageBase64;

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

	public List<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Long> roleIds) {
		this.roleIds = roleIds;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
}
