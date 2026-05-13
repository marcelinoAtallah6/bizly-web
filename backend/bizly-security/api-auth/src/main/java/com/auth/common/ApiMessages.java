package com.auth.common;

public class ApiMessages {
    public static final String USER_NOT_FOUND = "User not found";
    public static final String PASSWORD_PROCESSING_FAILED = "Failed to process password";
    public static final String MISSING_DEVICE_ID = "Device ID is required";
    public static final String INVALID_SESSION = "Invalid session";
    public static final String SESSION_INACTIVE = "Session inactive";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String DEVICE_MISMATCH = "Device mismatch";
    public static final String SESSION_EXPIRED = "Session expired";

    public static final String PASSWORD_REQUIRED = "newPassword is required";
    public static final String PASSWORD_TOO_WEAK = "Password does not meet security requirements : "
    		+ "At least 8 characters and include uppercase, lowercase, number and special character";
    public static final String PASSWORDS_DO_NOT_MATCH = "New password and confirmation must match";
    public static final String INVALID_RESET_TOKEN = "Invalid or expired reset token";

    /** Shown when the user has no role rows, or only broken FKs to deleted roles. */
    public static final String NO_ROLE_PROFILE = "No role profile is assigned to this account. An administrator must assign at least one role before you can sign in.";

    public static final String ACCOUNT_LOCKED = "This account is locked after too many failed sign-in attempts. Contact an administrator to unlock it.";

    public static final String INVALID_CREDENTIALS = "Invalid username or password";

    public static final String INVALID_ACTIVE_ROLE = "That role is not assigned to your account.";

    // ---- Business registration flow ----
    public static final String BUSINESS_NAME_REQUIRED = "Business name is required";
    public static final String BUSINESS_NAME_TAKEN = "A business with that name already exists";
    public static final String BUSINESS_REGISTRATION_NOT_ALLOWED = "Business registration is not allowed for this account";
    public static final String BUSINESS_ALREADY_REGISTERED = "This user is already linked to a business";
    public static final String BUSINESS_NOT_FOUND = "Business not found";

    /** Returned (and logged as a security event) when a request tries to claim a restricted role. */
    public static final String ROLE_NOT_ASSIGNABLE = "The requested role cannot be assigned through registration";
    public static final String ROLE_NOT_FOUND = "Role not found";
    public static final String NO_DEFAULT_REGISTRATION_ROLE = "No default registration role is configured. Contact an administrator.";

    // ---- Social login ----
    public static final String SOCIAL_PROVIDER_UNSUPPORTED = "Unsupported social provider";
    public static final String SOCIAL_TOKEN_INVALID = "Social login token is invalid or expired";

    // ---- Public sign-up (/auth/register) ----
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String LAST_NAME_REQUIRED = "Last name is required";
    public static final String USERNAME_TAKEN = "That username is already registered";
    public static final String EMAIL_TAKEN = "That email is already registered";
    public static final String FORBIDDEN_NOT_ADMIN = "This action requires a system administrator";
}