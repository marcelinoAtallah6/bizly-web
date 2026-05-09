package com.um.api.controller.menu;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.dto.menu.NavGroupItemResponse;
import com.um.api.service.menu.IMenuService;

@RestController
@RequestMapping("/menu")
public class MenuController {

	private static final Logger log = LogManager.getLogger(MenuController.class);

	@Autowired
	private IMenuService menuService;

	@PostMapping({ "/get" })
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<List<NavGroupItemResponse>> getMenus() {
		log.info("[UM_MENU][GET_MENUS]");
		return ResponseEntity.ok(menuService.getMenus());
	}
}
