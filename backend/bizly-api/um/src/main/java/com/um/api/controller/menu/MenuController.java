package com.um.api.controller.menu;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.dto.menu.NavGroupItemResponse;
import com.um.api.dto.menu.PermissionMetadataResponse;
import com.um.api.service.menu.IMenuService;
import com.um.api.service.menu.MenuPermissionMetadataService;
import com.um.common.ApiMessages;
import com.um.common.ApiResponse;

@RestController
@RequestMapping("/menu")
public class MenuController {

	private static final Logger log = LogManager.getLogger(MenuController.class);

	@Autowired
	private IMenuService menuService;

	@Autowired
	private MenuPermissionMetadataService permissionMetadataService;

	@PostMapping({ "/gets" })
	public ResponseEntity<List<NavGroupItemResponse>> getMenus() {
		log.info("[UM_MENU][GET_MENUS]");
		return ResponseEntity.ok(menuService.getMenus());
	}

	/**
	 * Routes and action codes for dynamic UI / tooling — every {@link com.um.api.service.security.RequireMenuPermission#menuRoute()}
	 * must match a {@code route} returned here (from {@code UM_MENUS}).
	 */
	@PostMapping("/permission-metadata")
	public ResponseEntity<ApiResponse<PermissionMetadataResponse>> permissionMetadata() {
		log.info("[UM_MENU][PERMISSION_METADATA]");
		return ResponseEntity
				.ok(ApiResponse.success(permissionMetadataService.buildMetadata(), ApiMessages.SUCCESS));
	}
}
