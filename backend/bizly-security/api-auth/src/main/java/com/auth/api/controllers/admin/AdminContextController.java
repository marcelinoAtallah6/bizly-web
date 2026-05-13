package com.auth.api.controllers.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.api.controllers.dto.admin.AdminBusinessDto;
import com.auth.api.controllers.dto.admin.AdminSearchRequest;
import com.auth.api.controllers.dto.admin.AdminUserDto;
import com.auth.api.service.admin.IAdminContextService;
import com.auth.common.ApiMessages;
import com.auth.config.Exception.ServiceException;
import com.auth.config.common.ApiResponse;

/**
 * SUPER_ADMIN "global context switcher" endpoints — only callable when the gateway-forwarded
 * {@code X-Role-Level} header equals {@code ADMIN}. Any other caller gets a 403, with a security
 * event line in the log so we can audit attempted access.
 */
@RestController
@RequestMapping("/auth/admin")
public class AdminContextController {

	private static final Logger log = LoggerFactory.getLogger(AdminContextController.class);
	private static final String ADMIN_LEVEL = "ADMIN";

	@Autowired
	private IAdminContextService service;

	@PostMapping("/businesses/search")
	public ResponseEntity<ApiResponse<List<AdminBusinessDto>>> searchBusinesses(
			@RequestBody AdminSearchRequest req, HttpServletRequest request) {
		requireAdmin(request);
		List<AdminBusinessDto> out = service.searchBusinesses(safe(req).getQ(), safe(req).getLimit());
		return ResponseEntity.ok(ApiResponse.success(out, "OK"));
	}

	@PostMapping("/users/search")
	public ResponseEntity<ApiResponse<List<AdminUserDto>>> searchUsers(
			@RequestBody AdminSearchRequest req, HttpServletRequest request) {
		requireAdmin(request);
		List<AdminUserDto> out = service.searchUsers(safe(req).getQ(), safe(req).getLimit());
		return ResponseEntity.ok(ApiResponse.success(out, "OK"));
	}

	private void requireAdmin(HttpServletRequest request) {
		String user = request.getHeader("X-User");
		String level = request.getHeader("X-Role-Level");
		if (user == null || user.isBlank()) {
			throw new ServiceException(ApiMessages.INVALID_SESSION, HttpStatus.UNAUTHORIZED);
		}
		if (level == null || !ADMIN_LEVEL.equalsIgnoreCase(level.trim())) {
			log.warn("[SECURITY_EVENT][ADMIN_CONTEXT_REJECTED] user={} level={} path={}",
					user, level, request.getRequestURI());
			throw new ServiceException(ApiMessages.FORBIDDEN_NOT_ADMIN, HttpStatus.FORBIDDEN);
		}
	}

	private static AdminSearchRequest safe(AdminSearchRequest req) {
		return req == null ? new AdminSearchRequest() : req;
	}
}
