package com.settings.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.settings.api.dto.reporting.ReportPageResponse;
import com.settings.api.dto.reporting.ReportRequest;
import com.settings.api.dto.reporting.ReportTypesResponse;
import com.settings.api.reporting.ReportingService;
import com.settings.audit.Audited;
import com.settings.common.ApiMessages;
import com.settings.common.ApiResponse;

/**
 * Reporting surface (under Settings).
 *
 * <ul>
 *   <li>{@code POST /reporting/getTypes} — what report categories are
 *       available, plus their filter + column metadata.</li>
 *   <li>{@code POST /reporting/generate} — paginated rows for one report.</li>
 *   <li>{@code POST /reporting/export} — same shape as generate but capped
 *       at {@code MAX_EXPORT_CAP} rows; the frontend turns these into
 *       CSV / Excel / PDF using its already-bundled libraries.</li>
 * </ul>
 */
@RestController
@RequestMapping("/reporting")
public class ReportingController {

	@Autowired
	private ReportingService reportingService;

	@PostMapping("/getTypes")
	public ResponseEntity<ApiResponse<ReportTypesResponse>> getTypes() {
		ReportTypesResponse body = new ReportTypesResponse(reportingService.listTypes());
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}

	@PostMapping("/generate")
	@Audited(action = "REPORTING_GENERATE", resourceType = "REPORT")
	public ResponseEntity<ApiResponse<ReportPageResponse>> generate(
			@RequestBody @Valid ReportRequest request) {
		ReportPageResponse body = reportingService.generate(request);
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}

	@PostMapping("/export")
	@Audited(action = "REPORTING_EXPORT", resourceType = "REPORT")
	public ResponseEntity<ApiResponse<ReportPageResponse>> export(
			@RequestBody @Valid ReportRequest request) {
		ReportPageResponse body = reportingService.export(request);
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}
}
