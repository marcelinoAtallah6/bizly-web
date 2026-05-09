package com.um.api.controller.role;

import java.util.List;

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

import com.um.api.dto.role.permission.GetRoleMenuPermissionsRequest;
import com.um.api.dto.role.permission.RoleMenuPermissionRowResponse;
import com.um.api.dto.role.permission.SaveRoleMenuPermissionsRequest;
import com.um.api.service.role.IRoleMenuPermissionService;
import com.um.common.ApiMessages;
import com.um.common.ApiResponse;

@RestController
@RequestMapping("/role/menu-permissions")
public class RoleMenuPermissionController {

	private static final Logger log = LogManager.getLogger(RoleMenuPermissionController.class);

	@Autowired
	private IRoleMenuPermissionService permissionService;

	@PostMapping("/get")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<List<RoleMenuPermissionRowResponse>>> get(
			@RequestBody @Valid GetRoleMenuPermissionsRequest request) {

		log.info("[UM_ROLE_MENU_PERM][GET] roleId={}", request.getRoleId());
		return ResponseEntity.ok(ApiResponse.success(permissionService.getPermissions(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/save")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponse<Void>> save(@RequestBody @Valid SaveRoleMenuPermissionsRequest request) {

		log.info("[UM_ROLE_MENU_PERM][SAVE] roleId={}", request.getRoleId());
		permissionService.savePermissions(request);
		return ResponseEntity.ok(ApiResponse.success(null, ApiMessages.SUCCESS));
	}
}
