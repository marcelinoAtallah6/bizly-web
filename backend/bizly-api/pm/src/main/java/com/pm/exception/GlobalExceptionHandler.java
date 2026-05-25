package com.pm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.pm.common.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
		// Return 403 Forbidden status
		return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldError().getDefaultMessage();

		// Return 400 Bad Request status
		return new ResponseEntity<>(ApiResponse.error("Validation Error: " + message), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {

		String customErrorMessage = ex.getMessage();

		return new ResponseEntity<>(ApiResponse.error(customErrorMessage), ex.getStatus());
	}

	/**
	 * {@code BusinessContextHolder.requireBusinessId()} throws {@link IllegalStateException} when
	 * the caller has no tenant context (admin in "all businesses" mode, or a user whose registration
	 * never finished). Returning 400 with the raw message gives the SPA something actionable to
	 * show — better than a generic 500 that looks like a bug.
	 */
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
		return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
		ex.printStackTrace();

		// Return 500 Internal Server Error status
		return new ResponseEntity<>(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}