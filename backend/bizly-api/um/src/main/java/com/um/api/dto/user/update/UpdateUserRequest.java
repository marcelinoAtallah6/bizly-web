package com.um.api.dto.user.update;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.um.common.ApiDefaultValdiation;

public class UpdateUserRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;

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
	@Pattern(regexp = "^\\+?[0-9]{7,15}$", message = ApiDefaultValdiation.MOBILE_NUMBER)
	private String mobileNumber;

	@NotBlank(message = ApiDefaultValdiation.STATUS)
	@Size(max = 50)
	private String status;

	@NotEmpty(message = ApiDefaultValdiation.ROLES)
	private List<Long> roleIds;

	/** When true, removes stored profile image (ignores base64 fields). */
	private Boolean clearProfileImage;

	private String profileImageMimeType;

	private String profileImageBase64;

	private LocalDate dateOfBirth;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Long> roleIds) {
		this.roleIds = roleIds;
	}

	public Boolean getClearProfileImage() {
		return clearProfileImage;
	}

	public void setClearProfileImage(Boolean clearProfileImage) {
		this.clearProfileImage = clearProfileImage;
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

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
}
