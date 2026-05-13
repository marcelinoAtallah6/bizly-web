package com.settings.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.settings.audit.Audited;
import com.settings.api.dto.favorite.DefaultRouteResponse;
import com.settings.api.dto.favorite.FavoriteRouteRequest;
import com.settings.api.dto.favorite.ReorderFavoritesRequest;
import com.settings.api.dto.favorite.SetDefaultFavoriteRequest;
import com.settings.api.dto.favorite.UserFavoritesResponse;
import com.settings.api.service.SettingsUserFavoritesService;
import com.settings.common.ApiMessages;
import com.settings.common.ApiResponse;

@RestController
@RequestMapping("/user-favorites")
public class UserFavoritesController {

	@Autowired
	private SettingsUserFavoritesService service;

	@PostMapping("/list")
	public ResponseEntity<ApiResponse<UserFavoritesResponse>> list() {
		UserFavoritesResponse body = service.list(SettingsSecuritySupport.currentUsername());
		return ResponseEntity.ok(ApiResponse.success(body, ApiMessages.SUCCESS));
	}

	@PostMapping("/default-route/get")
	public ResponseEntity<ApiResponse<DefaultRouteResponse>> getDefaultRoute() {
		String route = service.getDefaultRoute(SettingsSecuritySupport.currentUsername()).orElse(null);
		return ResponseEntity.ok(ApiResponse.success(new DefaultRouteResponse(route), ApiMessages.SUCCESS));
	}

	@PostMapping("/add")
	@Audited(action = "USER_FAV_ADD", resourceType = "USER_FAVORITE")
	public ResponseEntity<ApiResponse<Void>> add(@RequestBody @Valid FavoriteRouteRequest request) {
		service.add(SettingsSecuritySupport.currentUsername(), request.getRoute());
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}

	@PostMapping("/remove")
	@Audited(action = "USER_FAV_REMOVE", resourceType = "USER_FAVORITE")
	public ResponseEntity<ApiResponse<Void>> remove(@RequestBody @Valid FavoriteRouteRequest request) {
		service.remove(SettingsSecuritySupport.currentUsername(), request.getRoute());
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}

	@PostMapping("/set-default")
	@Audited(action = "USER_FAV_SET_DEFAULT", resourceType = "USER_FAVORITE")
	public ResponseEntity<ApiResponse<Void>> setDefault(@RequestBody SetDefaultFavoriteRequest request) {
		String route = request != null ? request.getRoute() : null;
		service.setDefault(SettingsSecuritySupport.currentUsername(), route);
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}

	@PostMapping("/reorder")
	@Audited(action = "USER_FAV_REORDER", resourceType = "USER_FAVORITE")
	public ResponseEntity<ApiResponse<Void>> reorder(@RequestBody ReorderFavoritesRequest request) {
		if (request != null) {
			service.reorder(SettingsSecuritySupport.currentUsername(), request.getRoutes());
		}
		return ResponseEntity.ok(ApiResponse.success(ApiMessages.SUCCESS));
	}
}
