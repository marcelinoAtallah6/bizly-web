package com.kyc.api.dto.update;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.kyc.api.dto.model.CustomerDetailRequest;
import com.kyc.common.ApiDefaultValdiation;

public class UpdateCustomerRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;

	@NotBlank(message = ApiDefaultValdiation.FIRST_NAME)
	@Size(max = 100)
	private String firstName;

	@NotBlank(message = ApiDefaultValdiation.LAST_NAME)
	@Size(max = 100)
	private String lastName;

	@NotNull(message = ApiDefaultValdiation.DOB)
	private LocalDate dob;

	@NotBlank(message = ApiDefaultValdiation.EMAIL)
	@Email(message = ApiDefaultValdiation.EMAIL)
	private String email;

	@NotBlank(message = ApiDefaultValdiation.MOBILE_NUMBER)
	@Pattern(regexp = "^\\+?[0-9]{7,15}$", message = ApiDefaultValdiation.MOBILE_NUMBER)
	private String mobileNumber;

	@Valid
	private List<CustomerDetailRequest> details;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
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

	public List<CustomerDetailRequest> getDetails() {
		return details;
	}

	public void setDetails(List<CustomerDetailRequest> details) {
		this.details = details;
	}
}
