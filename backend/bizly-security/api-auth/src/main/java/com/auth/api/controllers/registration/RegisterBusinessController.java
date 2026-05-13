package com.auth.api.controllers.registration;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.api.controllers.dto.registration.AssignableRoleDto;
import com.auth.api.controllers.dto.registration.MeAvatarResponse;
import com.auth.api.controllers.dto.registration.MeResponse;
import com.auth.api.controllers.dto.registration.RegisterBusinessRequest;
import com.auth.api.controllers.dto.registration.RegisterBusinessResponse;
import com.auth.api.controllers.dto.registration.RegisterRequest;
import com.auth.api.controllers.dto.registration.RegisterResponse;
import com.auth.api.service.registration.IRegisterBusinessService;
import com.auth.common.ApiMessages;
import com.auth.config.Exception.ServiceException;
import com.auth.config.common.ApiResponse;

/**
 * Endpoints supporting the secure business-registration flow.
 *
 * <p>Authentication model: api-auth has no JWT decoder filter of its own. The
 * Spring Cloud Gateway is the security boundary — it validates the JWT and
 * forwards an {@code X-User} header containing the verified username. These
 * endpoints therefore simply read {@code X-User} and refuse the request if it
 * is missing (which happens when the caller bypasses the gateway).</p>
 */
@RestController
@RequestMapping("/auth")
public class RegisterBusinessController {

	@Autowired
	private IRegisterBusinessService service;

	/**
	 * Public self-service sign-up. PUBLIC: no {@code X-User} required — this is what the
	 * "Create Account" wizard calls to bootstrap a brand-new tenant. The response includes a
	 * fully-issued JWT session so the SPA can land on the dashboard without a second login.
	 *
	 * Security: the controller does not honour any {@code roleId} field on the request. The
	 * server resolves the registration role from {@code um_role.is_default_for_registration}.
	 */
	@PostMapping("/register")
	public ResponseEntity<ApiResponse<RegisterResponse>> register(
			@RequestBody RegisterRequest req, HttpServletRequest request) {
		String deviceId = request.getHeader("X-DEVICE-ID");
		String ip = request.getRemoteAddr();
		RegisterResponse out = service.register(req, deviceId, ip);
		return ResponseEntity.ok(ApiResponse.success(out, "Account created successfully"));
	}

	@PostMapping("/register-business")
	public ResponseEntity<ApiResponse<RegisterBusinessResponse>> registerBusiness(
			@RequestBody RegisterBusinessRequest req, HttpServletRequest request) {

		String username = requireUser(request);
		String deviceId = request.getHeader("X-DEVICE-ID");
		String ip = request.getRemoteAddr();

		RegisterBusinessResponse out = service.registerBusiness(username, deviceId, ip, req);
		return ResponseEntity.ok(ApiResponse.success(out, "Business registered successfully"));
	}

	@PostMapping("/welcome-complete")
	public ResponseEntity<ApiResponse<RegisterBusinessResponse>> welcomeComplete(HttpServletRequest request) {
		String username = requireUser(request);
		String deviceId = request.getHeader("X-DEVICE-ID");
		String ip = request.getRemoteAddr();
		RegisterBusinessResponse out = service.completeWelcome(username, deviceId, ip);
		return ResponseEntity.ok(ApiResponse.success(out, "Welcome wizard completed"));
	}

	@PostMapping("/me")
	public ResponseEntity<ApiResponse<MeResponse>> me(HttpServletRequest request) {
		String username = requireUser(request);
		return ResponseEntity.ok(ApiResponse.success(service.me(username), "OK"));
	}

	/**
	 * Returns the current user's avatar bytes (mime + base64). Used by the navbar when the JWT can't
	 * embed the image. Cheap to call once per session — the SPA caches the result against the access
	 * token tail so the same user/token combo never re-fetches.
	 */
	@PostMapping("/me/avatar")
	public ResponseEntity<ApiResponse<MeAvatarResponse>> meAvatar(HttpServletRequest request) {
		String username = requireUser(request);
		return ResponseEntity.ok(ApiResponse.success(service.meAvatar(username), "OK"));
	}

	@PostMapping("/roles/assignable")
	public ResponseEntity<ApiResponse<List<AssignableRoleDto>>> assignableRoles(HttpServletRequest request) {
		requireUser(request);
		return ResponseEntity.ok(ApiResponse.success(service.listAssignableRoles(), "OK"));
	}

	/**
	 * Pulls the verified username from the gateway-injected header. Returning 401 here protects against
	 * the api-auth service being called directly (without the gateway in front of it).
	 */
	private String requireUser(HttpServletRequest request) {
		String u = request.getHeader("X-User");
		if (u == null || u.isBlank()) {
			throw new ServiceException(ApiMessages.INVALID_SESSION, HttpStatus.UNAUTHORIZED);
		}
		return u;
	}
}
