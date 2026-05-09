package com.auth.api.controllers.login;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.api.controllers.dto.forgot.ForgotPasswordRequest;
import com.auth.api.controllers.dto.forgot.ResetPasswordRequest;
import com.auth.api.controllers.dto.forgot.VerifyResetTokenRequest;
import com.auth.api.controllers.dto.refresh.RefreshRequest;
import com.auth.api.controllers.dto.session.ActiveRoleRequest;
import com.auth.api.model.login.LoginResponse;
import com.auth.api.service.login.ILoginService;
import com.auth.config.common.ApiResponse;

@RestController
@RequestMapping("/auth")
public class LoginController {

	@Autowired
	private ILoginService service;

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestParam("username") String userName,
			@RequestParam("password") String password) {
		return ResponseEntity
				.ok(ApiResponse.success(service.login(userName, password), "User Logged In successfully."));

	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody RefreshRequest req,
			HttpServletRequest request) {

		String deviceId = request.getHeader("X-DEVICE-ID");
		String ip = request.getRemoteAddr();

		LoginResponse response = service.refreshToken(req, deviceId, ip);

		return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
	}

	@PostMapping("/session/active-role")
	public ResponseEntity<ApiResponse<LoginResponse>> setActiveRole(@RequestBody ActiveRoleRequest req,
			HttpServletRequest request) {

		String deviceId = request.getHeader("X-DEVICE-ID");
		String ip = request.getRemoteAddr();

		LoginResponse response = service.setActiveRole(req, deviceId, ip);

		return ResponseEntity.ok(ApiResponse.success(response, "Active role updated"));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(@RequestParam("sessionId") String sessionId,
			HttpServletRequest request) {

		String deviceId = request.getHeader("X-DEVICE-ID");

		service.logout(sessionId, deviceId);

		return ResponseEntity.ok(ApiResponse.success("Logged out successfully", "SUCCESS"));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		service.forgotPassword(request);
		return ResponseEntity
				.ok(ApiResponse.success("If this username exists, password reset instructions will be processed.",
						"SUCCESS"));
	}

	@PostMapping("/forgot-password/verify")
	public ResponseEntity<ApiResponse<String>> verifyResetToken(@RequestBody VerifyResetTokenRequest request) {
		service.verifyResetToken(request);
		return ResponseEntity.ok(ApiResponse.success("Reset token is valid.", "SUCCESS"));
	}

	@PostMapping("/forgot-password/reset")
	public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
		service.resetPassword(request);
		return ResponseEntity.ok(ApiResponse.success("Password updated successfully.", "SUCCESS"));
	}

}
