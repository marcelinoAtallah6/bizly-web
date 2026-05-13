package com.bm.api.model.customer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bm.common.DatabaseConstants;

/**
 * Read-only view of {@code um.kyc_customer} for existence checks and display names.
 */
@Entity
@Table(name = DatabaseConstants.KYC_CUSTOMER_TABLE, schema = DatabaseConstants.SCHEMA)
public class KycCustomerRef {

	@Id
	private Long id;

	private String firstName;
	private String lastName;
	private String fullName;

	@Column(name = "customer_status")
	private String customerStatus;

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

	public String getCustomerStatus() {
		return customerStatus;
	}

	public void setCustomerStatus(String customerStatus) {
		this.customerStatus = customerStatus;
	}

	public String resolveDisplayName() {
		if (fullName != null && !fullName.isBlank()) {
			return fullName.trim();
		}
		String fn = firstName != null ? firstName.trim() : "";
		String ln = lastName != null ? lastName.trim() : "";
		String combined = (fn + " " + ln).trim();
		return combined.isEmpty() ? ("Customer #" + id) : combined;
	}
}
