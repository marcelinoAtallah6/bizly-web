package com.pm.common;

public class ApiDefaultValdiation {

	private ApiDefaultValdiation() {
	}
    public static final String REGEX_MOBILE_NUMBER = "\"^\\\\+?[0-9]{7,15}$\"";

	// Product
	public static final String ID = "ID is required";
	public static final String PAGE_NUMBER = "Page number must be zero or greater";
	public static final String PAGE_SIZE = "Page size must be at least 1";
	public static final String NAME = "Name is required";
	public static final String PRICE = "Price is required";
}