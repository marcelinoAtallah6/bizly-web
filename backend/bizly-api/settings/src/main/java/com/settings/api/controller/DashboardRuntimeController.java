package com.settings.api.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.settings.audit.Audited;
import com.settings.api.dto.dashboard.DashboardDetailResponse;
import com.settings.api.dto.dashboard.DashboardLoadRequest;
import com.settings.api.dto.dashboard.DashboardSummaryResponse;
import com.settings.api.dto.dashboard.LastDashboardResponse;
import com.settings.api.dto.dashboard.NavPreferenceRequest;
import com.settings.api.dto.dashboard.SetLastDashboardRequest;
import com.settings.api.dto.dashboard.WidgetDataRequest;
import com.settings.api.service.SettingsDashboardCatalogService;
import com.settings.api.service.SettingsNavPreferenceService;
import com.settings.api.service.SettingsUserPreferenceService;
import com.settings.api.service.SettingsWidgetDataService;
import com.settings.common.ApiMessages;
import com.settings.common.ApiResponse;

@RestController
@RequestMapping("/dashboard-runtime")
public class DashboardRuntimeController {

	@Autowired
	private SettingsDashboardCatalogService catalogService;

	@Autowired
	private SettingsWidgetDataService widgetDataService;

	@Autowired
	private SettingsNavPreferenceService navPreferenceService;

	@Autowired
	private SettingsUserPreferenceService userPreferenceService;

	@PostMapping("/for-user")
	public ResponseEntity<ApiResponse<List<DashboardSummaryResponse>>> forUser() {
		List<DashboardSummaryResponse> list = catalogService.listForNavbar(SettingsSecuritySupport.currentUsername(),
				SettingsSecuritySupport.currentAuthorities());
		return ResponseEntity.ok(ApiResponse.success(list, ApiMessages.SUCCESS));
	}

	@PostMapping("/load")
	public ResponseEntity<ApiResponse<DashboardDetailResponse>> load(@RequestBody @Valid DashboardLoadRequest request) {
		/*
		 * Resolve first so we can distinguish 404 (dashboard not found) from 403 (forbidden). Lumping both
		 * into 403 made the data-wipe / missing-grant scenario opaque to clients debugging the request.
		 */
		if (catalogService.resolveDashboard(request) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(ApiMessages.SETTINGS_DASHBOARD_NOT_FOUND));
		}
		DashboardDetailResponse detail = catalogService.loadForUser(request, SettingsSecuritySupport.currentUsername(),
				SettingsSecuritySupport.currentAuthorities());
		if (detail == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(ApiResponse.error(ApiMessages.SETTINGS_ACCESS_DENIED));
		}
		return ResponseEntity.ok(ApiResponse.success(detail, ApiMessages.SUCCESS));
	}

	@PostMapping("/widget-data")
	public ResponseEntity<ApiResponse<List<Map<String, Object>>>> widgetData(
			@RequestBody @Valid WidgetDataRequest request) {
		List<Map<String, Object>> rows = widgetDataService.executeWidgetQuery(SettingsSecuritySupport.currentUsername(),
				SettingsSecuritySupport.currentAuthorities(), request.getWidgetId());
		return ResponseEntity.ok(ApiResponse.success(rows, ApiMessages.SUCCESS));
	}

	@PostMapping("/nav-pref")
	@Audited(action = "SETTINGS_NAV_PREF", resourceType = "DASHBOARD_NAV")
	public ResponseEntity<ApiResponse<Void>> navPref(@RequestBody @Valid NavPreferenceRequest request) {
		navPreferenceService.setHidden(SettingsSecuritySupport.currentUsername(), request.getDashboardId(),
				Boolean.TRUE.equals(request.getHiddenNav()));
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}

	@PostMapping("/last-dashboard/get")
	public ResponseEntity<ApiResponse<LastDashboardResponse>> getLastDashboard() {
		LastDashboardResponse body = new LastDashboardResponse();
		userPreferenceService.getLastDashboardId(SettingsSecuritySupport.currentUsername(),
				SettingsSecuritySupport.currentAuthorities())
				.ifPresent(body::setDashboardId);
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}

	@PostMapping("/last-dashboard/set")
	@Audited(action = "SETTINGS_LAST_DASHBOARD", resourceType = "USER_PREF")
	public ResponseEntity<ApiResponse<Void>> setLastDashboard(@RequestBody SetLastDashboardRequest request) {
		userPreferenceService.setLastDashboardId(SettingsSecuritySupport.currentUsername(),
				SettingsSecuritySupport.currentAuthorities(), request.getId());
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}
}
