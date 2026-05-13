package com.settings.common;

public class ApiMessages {

	private ApiMessages() {
	}

	public static final String SUCCESS = "Request processed successfully";

	public static final String USER_ADDED = "User added successfully";
	public static final String USER_UPDATED = "User updated successfully";
	public static final String USER_DELETED = "User deleted successfully";
	public static final String USER_NOT_FOUND = "User not found";

	// Role
	public static final String ROLE_ADDED = "Role added successfully";
	public static final String ROLE_UPDATED = "Role updated successfully";
	public static final String ROLE_DELETED = "Role deleted successfully";
	public static final String ROLE_NOT_FOUND = "Role not found";

	// Password
	public static final String PASSWORD_PROCESSING_FAILED = "Failed to process password";
	public static final String PASSWORD_TOO_WEAK = "Password does not meet security requirements";

	public static final String INVALID_PROFILE_IMAGE = "Invalid or unsupported profile image";
	public static final String PROFILE_IMAGE_TOO_LARGE = "Profile image exceeds maximum size";

	public static final String MENU_PERMISSION_DENIED = "You do not have permission for this action";

	public static final String SETTINGS_QUERY_NOT_FOUND = "Saved query not found";
	public static final String SETTINGS_QUERY_NAME_EXISTS = "A query with this name already exists";
	public static final String SETTINGS_DASHBOARD_NOT_FOUND = "Dashboard not found";
	public static final String SETTINGS_DASHBOARD_SLUG_EXISTS = "A dashboard with this slug already exists";
	public static final String SETTINGS_ACCESS_DENIED = "You do not have access to this dashboard";
	public static final String SETTINGS_WIDGET_NOT_FOUND = "Widget not found";

	public static final String REPORTING_TYPE_NOT_FOUND = "Report type not found";
	public static final String REPORTING_REPORT_NOT_FOUND = "Report not found";
	public static final String REPORTING_NAME_EXISTS = "A report with this name already exists";
	public static final String REPORTING_CODE_EXISTS = "A report with this code already exists";
	public static final String REPORTING_INVALID = "Invalid report payload";
}

