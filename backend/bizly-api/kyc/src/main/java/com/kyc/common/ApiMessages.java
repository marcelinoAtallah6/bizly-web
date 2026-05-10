package com.kyc.common;

public class ApiMessages {

	private ApiMessages() {
	}

	public static final String SUCCESS = "Request processed successfully";

	// Product
	public static final String PRODUCT_ADDED = "Product added successfully";
	public static final String PRODUCT_UPDATED = "Product updated successfully";
	public static final String PRODUCT_DELETED = "Product deleted successfully";

	// Customer
	public static final String CUSTOMER_ADDED = "Customer added successfully";
	public static final String CUSTOMER_UPDATED = "Customer updated successfully";
	public static final String CUSTOMER_DELETED = "Customer deleted successfully";
	public static final String CUSTOMER_NOT_FOUND = "Customer not found";

	public static final String MENU_PERMISSION_DENIED = "You do not have permission for this action";

}