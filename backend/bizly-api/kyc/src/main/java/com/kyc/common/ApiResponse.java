package com.kyc.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private final boolean success;
	private final String message;
	private final T data;

	private ApiResponse(T data, String message) {
		this.success = true;
		this.message = message;
		this.data = data;
	}

	// --- Private Constructor for failure/error ---
	private ApiResponse(String message) {
		this.success = false;
		this.message = message;
		this.data = null;
	}

	public static <T> ApiResponse<T> success(T data, String message) {
		return new ApiResponse<>(data, message);
	}

	public static <T> ApiResponse<T> success(String message) {
		return new ApiResponse<>(null, message);
	}

	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>(message);
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}
}