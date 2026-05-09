package com.kyc.api.dto.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.kyc.common.ApiDefaultValdiation;

public class CustomerDetailRequest {

	@NotBlank(message = ApiDefaultValdiation.FIELD_NAME)
	@Size(max = 100)
	private String fieldName;

	@NotBlank(message = ApiDefaultValdiation.FIELD_VALUE)
	@Size(max = 500)
	private String fieldValue;

	@NotBlank(message = ApiDefaultValdiation.CUSTOMER_STATUS)
	@Size(max = 80)
	private String customerStatus;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public String getCustomerStatus() {
		return customerStatus;
	}

	public void setCustomerStatus(String customerStatus) {
		this.customerStatus = customerStatus;
	}
}
