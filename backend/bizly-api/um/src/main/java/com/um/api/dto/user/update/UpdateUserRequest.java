package com.um.api.dto.user.update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
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
}
