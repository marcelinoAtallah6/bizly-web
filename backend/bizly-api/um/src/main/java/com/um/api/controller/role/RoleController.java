package com.um.api.controller.role;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.dto.role.add.AddRoleRequest;
import com.um.api.dto.role.add.AddRoleResponse;
import com.um.api.dto.role.delete.DeleteRoleRequest;
import com.um.api.dto.role.delete.DeleteRoleResponse;
import com.um.api.dto.role.get.GetRoleRequest;
import com.um.api.dto.role.get.GetRoleResponse;
import com.um.api.dto.role.gets.GetsRolesRequest;
import com.um.api.dto.role.update.UpdateRoleRequest;
import com.um.api.dto.role.update.UpdateRoleResponse;
import com.um.api.service.role.IRoleService;
import com.um.common.ApiMessages;
import com.um.common.ApiResponse;
import com.um.common.PageResponse;

@RestController
@RequestMapping("/role")
public class RoleController {

	private static final Logger log = LogManager.getLogger(RoleController.class);

	@Autowired
	private IRoleService service;

	@PostMapping("/add")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<AddRoleResponse>> add(@Valid @RequestBody AddRoleRequest request) {
		log.info("[UM_ROLE][ADD] name={}", request.getName());
		AddRoleResponse response = service.add(request);
		return ResponseEntity.ok(ApiResponse.success(response, ApiMessages.ROLE_ADDED));
	}

	@PostMapping("/update")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<UpdateRoleResponse>> update(@Valid @RequestBody UpdateRoleRequest request) {
		log.info("[UM_ROLE][UPDATE] id={}", request.getId());
		UpdateRoleResponse response = service.update(request);
		return ResponseEntity.ok(ApiResponse.success(response, ApiMessages.ROLE_UPDATED));
	}

	@PostMapping("/delete")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<DeleteRoleResponse>> delete(@Valid @RequestBody DeleteRoleRequest request) {
		log.info("[UM_ROLE][DELETE] id={}", request.getId());
		DeleteRoleResponse response = service.delete(request);
		return ResponseEntity.ok(ApiResponse.success(response, ApiMessages.ROLE_DELETED));
	}

	@PostMapping("/get")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<GetRoleResponse>> get(@Valid @RequestBody GetRoleRequest request) {
		log.info("[UM_ROLE][GET] id={}", request.getId());
		GetRoleResponse response = service.get(request);
		return ResponseEntity.ok(ApiResponse.success(response, ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<PageResponse<GetRoleResponse>>> gets(
			@Valid @RequestBody GetsRolesRequest request) {
		log.info("[UM_ROLE][GETS] page={}", request.getPageNumber());
		PageResponse<GetRoleResponse> response = service.gets(request);
		return ResponseEntity.ok(ApiResponse.success(response, ApiMessages.SUCCESS));
	}
}