package com.broadcast.common;

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

	public static final String MENU_PERMISSION_DENIED = "You do not have permission for this action";

	// Service item
	public static final String SERVICE_ITEM_ADDED = "Service added successfully";
	public static final String SERVICE_ITEM_UPDATED = "Service updated successfully";
	public static final String SERVICE_ITEM_DEACTIVATED = "Service deactivated successfully";
	public static final String SERVICE_ITEM_NOT_FOUND = "Service not found";
	public static final String SERVICE_ITEM_INACTIVE = "Service is inactive and cannot be used for appointments";

	// Appointment
	public static final String APPOINTMENT_ADDED = "Appointment created successfully";
	public static final String APPOINTMENT_UPDATED = "Appointment updated successfully";
	public static final String APPOINTMENT_CANCELLED = "Appointment cancelled successfully";
	public static final String APPOINTMENT_NOT_FOUND = "Appointment not found";
	public static final String CUSTOMER_NOT_FOUND = "Customer not found";
	public static final String APPOINTMENT_OVERLAP = "This customer already has an overlapping appointment in that time range";

	public static final String BROADCAST_CREATED = "Broadcast message saved";
	public static final String BROADCAST_QUEUED = "Broadcast queued for delivery";
	public static final String BROADCAST_NOT_FOUND = "Broadcast not found";
	public static final String BROADCAST_INVALID_STATE = "Broadcast cannot be sent in its current state";
	public static final String BROADCAST_VALIDATION = "Broadcast request is invalid";
	public static final String BROADCAST_ALREADY_QUEUED = "Broadcast is already queued or in progress";
	public static final String BROADCAST_RESEND_NOT_ALLOWED = "Failed broadcasts cannot be re-queued; create a new message";

	public static final String BROADCAST_ADMIN_REQUIRED = "Only administrators may manage broadcasts";

}