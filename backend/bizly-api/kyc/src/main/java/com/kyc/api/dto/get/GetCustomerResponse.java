package com.kyc.api.dto.get;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.kyc.api.dto.model.CustomerDetailResponse;

public class GetCustomerResponse {

	private Long id;
	private String firstName;
	private String lastName;
	private String fullName;
	private LocalDate dob;
	private String email;
	private String mobileNumber;
	private LocalDateTime createdAt;
	private List<CustomerDetailResponse> details;

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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<CustomerDetailResponse> getDetails() {
		return details;
	}

	public void setDetails(List<CustomerDetailResponse> details) {
		this.details = details;
	}
}
