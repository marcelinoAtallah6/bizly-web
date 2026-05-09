package com.pm.common;

public class ApiMessages {

	private ApiMessages() {
	}

	public static final String SUCCESS = "Request processed successfully";

	// Product
	public static final String PRODUCT_ADDED = "Product added successfully";
	public static final String PRODUCT_UPDATED = "Product updated successfully";
	public static final String PRODUCT_DELETED = "Product deleted successfully";
	public static final String PRODUCT_NOT_FOUND = "Product not found";

	public static final String INVALID_PRODUCT_IMAGE = "Invalid or unsupported product image";
	public static final String PRODUCT_IMAGE_TOO_LARGE = "Product image exceeds maximum size";

	public static final String SALE_COMPLETED = "Sale recorded successfully";
	public static final String SALE_NOT_FOUND = "Sale not found";
	public static final String CHECKOUT_INVALID = "Checkout request is invalid";
	public static final String INSUFFICIENT_STOCK = "Insufficient stock for one or more products";

}