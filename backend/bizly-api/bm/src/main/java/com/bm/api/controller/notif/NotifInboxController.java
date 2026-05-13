package com.bm.api.controller.notif;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bm.api.dto.notif.MarkReadRequest;
import com.bm.api.dto.notif.NotifInboxListResponse;
import com.bm.api.dto.notif.NotifInboxRecentRequest;
import com.bm.api.service.notif.INotifInboxService;
import com.bm.api.service.notif.NotifCategory;
import com.bm.api.service.notif.NotifPublishRequest;
import com.bm.api.service.notif.NotifSeverity;
import com.bm.common.ApiMessages;
import com.bm.common.ApiResponse;

@RestController
@RequestMapping("/notif-inbox")
public class NotifInboxController {

	private static final Logger log = LogManager.getLogger(NotifInboxController.class);

	@Autowired
	private INotifInboxService service;

	@PostMapping("/recent")
	public @ResponseBody ResponseEntity<ApiResponse<NotifInboxListResponse>> recent(HttpServletRequest httpRequest,
			@RequestBody(required = false) NotifInboxRecentRequest request) {

		String user = httpRequest.getHeader("X-User");
		Integer pageNumber = request != null ? request.getPageNumber() : null;
		Integer pageSize = request != null ? request.getPageSize() : null;
		log.debug("[NOTIF_INBOX][RECENT] user={} page={} size={}", user, pageNumber, pageSize);

		NotifInboxListResponse body = service.recent(user, pageNumber, pageSize);
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}

	@PostMapping("/unread-count")
	public @ResponseBody ResponseEntity<ApiResponse<Long>> unreadCount(HttpServletRequest httpRequest) {
		String user = httpRequest.getHeader("X-User");
		long c = service.unreadCount(user);
		return ResponseEntity.ok(ApiResponse.success(c, ApiMessages.SUCCESS));
	}

	@PostMapping("/mark-read")
	public @ResponseBody ResponseEntity<ApiResponse<Void>> markRead(HttpServletRequest httpRequest,
			@RequestBody MarkReadRequest request) {
		String user = httpRequest.getHeader("X-User");
		if (request != null) {
			service.markRead(user, request.getId());
		}
		return ResponseEntity.ok(ApiResponse.success(null, ApiMessages.SUCCESS));
	}

	@PostMapping("/mark-all-read")
	public @ResponseBody ResponseEntity<ApiResponse<Void>> markAllRead(HttpServletRequest httpRequest) {
		String user = httpRequest.getHeader("X-User");
		service.markAllRead(user);
		return ResponseEntity.ok(ApiResponse.success(null, ApiMessages.SUCCESS));
	}

	/**
	 * Diagnostic-only endpoint: publishes a single test row to the calling user
	 * so the bell, badge, and dropdown can be verified end-to-end without
	 * having to book an appointment. Safe to call repeatedly — each call adds
	 * one more row. Remove or guard behind a profile flag once the feature is
	 * fully verified in production.
	 */
	@PostMapping("/seed-test")
	public @ResponseBody ResponseEntity<ApiResponse<String>> seedTest(HttpServletRequest httpRequest) {
		String user = httpRequest.getHeader("X-User");
		log.info("[NOTIF_INBOX][SEED_TEST] user={}", user);

		if (user == null || user.isBlank()) {
			return ResponseEntity.ok(ApiResponse.error("X-User header missing — gateway did not forward identity."));
		}

		boolean written = service.publish(NotifPublishRequest
				.of(user, NotifCategory.SYSTEM, NotifSeverity.INFO, "Inbox is wired up")
				.body("This is a test notification — bell, badge, and dropdown are all working.")
				.link("/dashboard")
				.resource("SELF_TEST", String.valueOf(System.currentTimeMillis())));

		if (!written) {
			// Most likely the DDL patch hasn't been applied — the server log will
			// have the specific reason. Return a hard error so the frontend
			// surfaces it via snackbar instead of pretending it worked.
			return ResponseEntity.ok(ApiResponse.error(
					"Publish failed. Most likely UM.NOTIF_INBOX does not exist yet — run "
							+ "patch-notif-inbox-oracle.sql, restart the bm service, then try again. "
							+ "Check the bm log for [NOTIF_INBOX][PUBLISH][FAIL]."));
		}
		return ResponseEntity.ok(ApiResponse.success(
				"Published 1 test notification to " + user, ApiMessages.SUCCESS));
	}
}
