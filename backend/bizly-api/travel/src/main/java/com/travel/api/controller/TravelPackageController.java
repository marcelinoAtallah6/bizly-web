package com.travel.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.travel.api.dto.packagepkg.TravelPackageDtos.*;
import com.travel.api.service.packagepkg.ITravelPackageService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/package")
public class TravelPackageController {

	private static final Logger log = LogManager.getLogger(TravelPackageController.class);

	@Autowired
	private ITravelPackageService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/packages", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelPackageResponse>> add(HttpServletRequest httpRequest,
			@RequestBody @Valid AddTravelPackageRequest request) {
		log.info("[TRAVEL_PACKAGE][ADD] name={}", request.getName());
		return ResponseEntity.ok(ApiResponse.success(service.add(request, httpRequest.getHeader("X-User")),
				ApiMessages.PACKAGE_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/packages", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelPackageResponse>> update(HttpServletRequest httpRequest,
			@RequestBody @Valid UpdateTravelPackageRequest request) {
		log.info("[TRAVEL_PACKAGE][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request, httpRequest.getHeader("X-User")),
				ApiMessages.PACKAGE_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/packages", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelPackageResponse>> delete(
			@RequestBody @Valid DeleteTravelPackageRequest request) {
		log.info("[TRAVEL_PACKAGE][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.PACKAGE_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/packages", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelPackageResponse>> get(
			@RequestBody @Valid GetTravelPackageRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/packages", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelPackageResponse>>> gets(
			@RequestBody @Valid GetsTravelPackagesRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
