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
	public static final String SALE_ID_REQUIRED = "Sale id is required";
	public static final String SALE_NOT_FOUND = "Sale not found";
	public static final String CHECKOUT_INVALID = "Checkout request is invalid";
	public static final String INSUFFICIENT_STOCK = "Insufficient stock for one or more products";

	/**
	 * Mixed product+service checkout: a line is invalid if it points at a missing service item or a
	 * deactivated one. PM enforces the same rules BM used to so callers see consistent errors after
	 * the {@code /pm/sale/checkout} move.
	 */
	public static final String SERVICE_ITEM_NOT_FOUND = "Service item not found";
	public static final String SERVICE_ITEM_INACTIVE = "Service item is inactive";

	public static final String MENU_PERMISSION_DENIED = "You do not have permission for this action";

}