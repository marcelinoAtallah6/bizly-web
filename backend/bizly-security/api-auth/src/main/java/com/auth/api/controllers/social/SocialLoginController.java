package com.auth.api.controllers.social;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.api.controllers.dto.social.SocialLoginRequest;
import com.auth.api.model.login.LoginResponse;
import com.auth.api.service.social.ISocialLoginService;
import com.auth.config.common.ApiResponse;

/**
 * Public endpoints that accept a provider-issued token from the SPA, verify it
 * server-side, and return a Bizly session (JWT + refresh) via the same pipeline
 * as password login.
 */
@RestController
@RequestMapping("/auth/social")
public class SocialLoginController {

	@Autowired
	private ISocialLoginService service;

	@PostMapping("/{provider}")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@PathVariable("provider") String provider,
			@RequestBody SocialLoginRequest req, HttpServletRequest request) {

		String deviceId = request.getHeader("X-DEVICE-ID");
		String ip = request.getRemoteAddr();
		LoginResponse session = service.login(provider, req, deviceId, ip);
		return ResponseEntity.ok(ApiResponse.success(session, "Signed in via " + provider));
	}
}
