package com.settings.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.settings.common.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
		return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
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

	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {

		String customErrorMessage = ex.getMessage();

		return new ResponseEntity<>(ApiResponse.error(customErrorMessage), ex.getStatus());
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException ex) {
		log.error("Database error", ex);
		return new ResponseEntity<>(ApiResponse.error("A database error occurred. Please try again later."),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
		log.error("Unexpected error", ex);

		return new ResponseEntity<>(ApiResponse.error("An unexpected error occurred. Please try again later."),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}