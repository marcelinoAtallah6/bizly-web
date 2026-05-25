package com.settings.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.settings.api.dto.reporting.ActiveReportRef;
import com.settings.api.service.SettingsReportService;
import com.settings.common.ApiMessages;
import com.settings.common.ApiResponse;

/**
 * Read-only reporting for end users ({@code /reports} menu). Lists ACTIVE reports
 * assigned to the caller via {@code SETTINGS_REPORT_ROLE_GRANT} / {@code SETTINGS_REPORT_USER_GRANT}.
 * Create/update/delete remain under {@link ReportBuilderController}.
 */
@RestController
@RequestMapping("/reporting/viewer")
public class ReportsViewerController {

	@Autowired
	private SettingsReportService reportService;

	@PostMapping("/list")
	public ResponseEntity<ApiResponse<List<ActiveReportRef>>> listAssigned() {
		List<ActiveReportRef> body = reportService.listActiveForSidebar(
				SettingsSecuritySupport.currentUsername(),
				SettingsSecuritySupport.currentAuthorities());
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}
}
