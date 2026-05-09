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
}