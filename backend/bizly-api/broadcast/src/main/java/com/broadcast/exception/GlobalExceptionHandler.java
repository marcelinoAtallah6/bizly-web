package com.broadcast.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.broadcast.common.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
		// Return 403 Forbidden status
		return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		FieldError fe = ex.getBindingResult().getFieldError();
		String message = fe != null ? fe.getDefaultMessage() : ex.getMessage();

		// Return 400 Bad Request status
		return new ResponseEntity<>(ApiResponse.error("Validation Error: " + message), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {

		String customErrorMessage = ex.getMessage();

		return new ResponseEntity<>(ApiResponse.error(customErrorMessage), ex.getStatus());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
		ex.printStackTrace();

		// Return 500 Internal Server Error status
		return new ResponseEntity<>(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}