package com.settings.api.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.settings.audit.Audited;
import com.settings.api.dto.query.QueryDefGetsRequest;
import com.settings.api.dto.query.QueryDefIdRequest;
import com.settings.api.dto.query.QueryDefResponse;
import com.settings.api.dto.query.QueryDefSaveRequest;
import com.settings.api.dto.query.QueryExecuteTestRequest;
import com.settings.api.dto.query.QueryValidateRequest;
import com.settings.api.service.SettingsJdbcQueryService;
import com.settings.api.service.SettingsQueryDefService;
import com.settings.api.service.SqlQueryValidationService;
import com.settings.common.ApiMessages;
import com.settings.common.ApiResponse;
import com.settings.common.PageResponse;

@RestController
@RequestMapping("/query-def")
public class QueryDefinitionController {

	@Autowired
	private SettingsQueryDefService queryDefService;

	@Autowired
	private SqlQueryValidationService validationService;

	@Autowired
	private SettingsJdbcQueryService jdbcQueryService;

	@PostMapping("/gets")
	public ResponseEntity<ApiResponse<PageResponse<QueryDefResponse>>> gets(
			@RequestBody @Valid QueryDefGetsRequest request) {
		PageResponse<QueryDefResponse> body = queryDefService.listPage(request,
				SettingsSecuritySupport.currentUsername(),
				SettingsSecuritySupport.currentAuthorities());
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}

	@PostMapping("/get")
	public ResponseEntity<ApiResponse<QueryDefResponse>> get(@RequestBody @Valid QueryDefIdRequest request) {
		return ResponseEntity.ok(ApiResponse.success(queryDefService.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/save")
	@Audited(action = "SETTINGS_QUERY_SAVE", resourceType = "QUERY_DEF")
	public ResponseEntity<ApiResponse<QueryDefResponse>> save(@RequestBody @Valid QueryDefSaveRequest request) {
		QueryDefResponse saved = queryDefService.save(request, SettingsSecuritySupport.currentUsername());
		return ResponseEntity.ok(ApiResponse.success(saved, ApiMessages.SUCCESS));
	}

	@PostMapping("/delete")
	@Audited(action = "SETTINGS_QUERY_DELETE", resourceType = "QUERY_DEF")
	public ResponseEntity<ApiResponse<Void>> delete(@RequestBody @Valid QueryDefIdRequest request) {
		queryDefService.delete(request);
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}

	@PostMapping("/validate")
	@Audited(action = "SETTINGS_QUERY_VALIDATE", resourceType = "QUERY_DEF")
	public ResponseEntity<ApiResponse<Void>> validate(@RequestBody @Valid QueryValidateRequest request) {
		validationService.validateSelectOnly(request.getSqlText());
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}

	@PostMapping("/execute-test")
	@Audited(action = "SETTINGS_QUERY_EXECUTE_TEST", resourceType = "QUERY_DEF")
	public ResponseEntity<ApiResponse<List<Map<String, Object>>>> executeTest(
			@RequestBody @Valid QueryExecuteTestRequest request) {
		List<Map<String, Object>> rows = jdbcQueryService.executeSelect(request.getSqlText());
		return ResponseEntity.ok(ApiResponse.success(rows, ApiMessages.SUCCESS));
	}
}
