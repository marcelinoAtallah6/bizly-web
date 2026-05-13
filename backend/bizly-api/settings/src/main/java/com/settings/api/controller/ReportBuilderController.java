package com.settings.api.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.settings.api.dto.reporting.ActiveReportRef;
import com.settings.api.dto.reporting.ReportBuilderIdRequest;
import com.settings.api.dto.reporting.ReportBuilderItem;
import com.settings.api.dto.reporting.ReportBuilderListRequest;
import com.settings.api.dto.reporting.ReportBuilderSaveRequest;
import com.settings.api.service.SettingsReportService;
import com.settings.audit.Audited;
import com.settings.common.ApiMessages;
import com.settings.common.ApiResponse;
import com.settings.common.PageResponse;

/**
 * Admin API for the Report Builder. All endpoints are POST so the gateway and
 * audit aspect stay uniform with the rest of the settings module.
 */
@RestController
@RequestMapping("/reporting/builder")
public class ReportBuilderController {

	@Autowired
	private SettingsReportService reportService;

	@PostMapping("/list")
	public ResponseEntity<ApiResponse<PageResponse<ReportBuilderItem>>> list(
			@RequestBody @Valid ReportBuilderListRequest request) {
		PageResponse<ReportBuilderItem> body = reportService.list(
				request.getPageNumber(), request.getPageSize(), request.getNameSearch(),
				SettingsSecuritySupport.currentUsername(),
				SettingsSecuritySupport.currentAuthorities());
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}

	@PostMapping("/get")
	public ResponseEntity<ApiResponse<ReportBuilderItem>> get(
			@RequestBody @Valid ReportBuilderIdRequest request) {
		ReportBuilderItem item = reportService.get(request.getId());
		return ResponseEntity.ok(ApiResponse.success(item, ApiMessages.SUCCESS));
	}

	@PostMapping("/save")
	@Audited(action = "REPORTING_REPORT_SAVE", resourceType = "REPORT")
	public ResponseEntity<ApiResponse<ReportBuilderItem>> save(
			@RequestBody @Valid ReportBuilderSaveRequest request) {
		ReportBuilderItem saved = reportService.save(request, SettingsSecuritySupport.currentUsername());
		return ResponseEntity.ok(ApiResponse.success(saved, ApiMessages.SUCCESS));
	}

	@PostMapping("/delete")
	@Audited(action = "REPORTING_REPORT_DELETE", resourceType = "REPORT")
	public ResponseEntity<ApiResponse<Void>> delete(
			@RequestBody @Valid ReportBuilderIdRequest request) {
		reportService.delete(request.getId());
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}

	@PostMapping("/listActive")
	public ResponseEntity<ApiResponse<List<ActiveReportRef>>> listActive() {
		List<ActiveReportRef> body = reportService.listActiveForSidebar(
				SettingsSecuritySupport.currentUsername(),
				SettingsSecuritySupport.currentAuthorities());
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}
}
