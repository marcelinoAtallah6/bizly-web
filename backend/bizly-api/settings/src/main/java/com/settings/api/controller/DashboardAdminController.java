package com.settings.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.settings.audit.Audited;
import com.settings.api.dto.dashboard.DashboardDetailResponse;
import com.settings.api.dto.dashboard.DashboardIdRequest;
import com.settings.api.dto.dashboard.DashboardSaveRequest;
import com.settings.api.dto.dashboard.DashboardSummaryResponse;
import com.settings.api.service.SettingsDashboardCatalogService;
import com.settings.api.service.SettingsDashboardManagementService;
import com.settings.common.ApiMessages;
import com.settings.common.ApiResponse;

@RestController
@RequestMapping("/dashboard-admin")
public class DashboardAdminController {

	@Autowired
	private SettingsDashboardManagementService managementService;

	@Autowired
	private SettingsDashboardCatalogService catalogService;

	@PostMapping("/definitions")
	public ResponseEntity<ApiResponse<java.util.List<DashboardSummaryResponse>>> definitions() {
		return ResponseEntity.ok(ApiResponse.success(catalogService.listAllDefinitions(), ApiMessages.SUCCESS));
	}

	@PostMapping("/save")
	@Audited(action = "SETTINGS_DASHBOARD_SAVE", resourceType = "DASHBOARD")
	public ResponseEntity<ApiResponse<DashboardDetailResponse>> save(@RequestBody @Valid DashboardSaveRequest request) {
		return ResponseEntity.ok(ApiResponse.success(managementService.save(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/delete")
	@Audited(action = "SETTINGS_DASHBOARD_DELETE", resourceType = "DASHBOARD")
	public ResponseEntity<ApiResponse<Void>> delete(@RequestBody @Valid DashboardIdRequest request) {
		managementService.delete(request);
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}
}
