package com.um.api.controller.audit;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.dto.audit.AuditLogRowResponse;
import com.um.api.dto.audit.GetsAuditLogsRequest;
import com.um.api.service.audit.IAuditLogService;
import com.um.common.ApiMessages;
import com.um.common.ApiResponse;
import com.um.common.PageResponse;

@RestController
@RequestMapping("/audit")
public class AuditController {

	private static final Logger log = LogManager.getLogger(AuditController.class);

	@Autowired
	private IAuditLogService auditLogService;

	@PostMapping("/gets")
	@PreAuthorize("hasRole('USER')")
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<AuditLogRowResponse>>> gets(
			@RequestBody @Valid GetsAuditLogsRequest request) {

		log.info("[UM_AUDIT][GETS] page={} size={}", request.getPageNumber(), request.getPageSize());
		return ResponseEntity.ok(ApiResponse.success(auditLogService.search(request), ApiMessages.SUCCESS));
	}
}
