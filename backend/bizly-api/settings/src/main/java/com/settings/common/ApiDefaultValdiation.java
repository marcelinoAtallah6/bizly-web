package com.settings.common;

public class ApiDefaultValdiation {

	private ApiDefaultValdiation() {
	}

    public static final String REGEX_MOBILE_NUMBER = "\"^\\\\+?[0-9]{7,15}$\"";


	public static final String ID = "ID is required";
	public static final String FIRST_NAME = "First name is required";
	public static final String LAST_NAME = "Last name is required";
	public static final String EMAIL = "Valid email is required";
	public static final String MOBILE_NUMBER = "Valid mobile number is required";
	public static final String USERNAME = "Username is required";
	public static final String STATUS = "Status is required";
	public static final String PAGE_NUMBER = "Page number must be zero or greater";
	public static final String PAGE_SIZE = "Page size must be at least 1";
    public static final String PASSWORD = "Password is required";
    public static final String ROLE_NAME = "Role name is required";
    public static final String ROLE_TYPE = "Role type is required";
    public static final String ROLES = "Roles are required";
}
