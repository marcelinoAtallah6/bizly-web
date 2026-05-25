package com.um.api.controller.user;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.dto.user.profile.UpdateProfileSelfRequest;
import com.um.api.dto.user.add.AddUserRequest;
import com.um.api.dto.user.add.AddUserResponse;
import com.um.api.dto.user.delete.DeleteUserRequest;
import com.um.api.dto.user.delete.DeleteUserResponse;
import com.um.api.dto.user.get.GetUserRequest;
import com.um.api.dto.user.get.GetUserResponse;
import com.um.api.dto.user.gets.GetsUsersRequest;
import com.um.api.dto.user.update.UpdateUserRequest;
import com.um.api.dto.user.update.UpdateUserResponse;
import com.um.api.audit.Audited;
import com.um.api.service.security.MenuPermissionAction;
import com.um.api.service.security.RequireMenuPermission;
import com.um.api.service.user.IUserService;
import com.um.common.ApiMessages;
import com.um.common.ApiResponse;
import com.um.common.PageResponse;
import com.um.exception.ServiceException;

@RestController
@RequestMapping("/user")
public class UserController {

	private static final Logger log = LogManager.getLogger(UserController.class);

	@Autowired
	private IUserService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/um/user", action = MenuPermissionAction.ADD)
	@Audited(action = "UM_USER_ADD", resourceType = "USER")
	public @ResponseBody ResponseEntity<ApiResponse<AddUserResponse>> add(@RequestBody @Valid AddUserRequest request) {

		log.info("[UM_USER][ADD] username={}", request.getUsername());
		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.USER_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/um/user", action = MenuPermissionAction.EDIT)
	@Audited(action = "UM_USER_UPDATE", resourceType = "USER")
	public @ResponseBody ResponseEntity<ApiResponse<UpdateUserResponse>> update(
			@RequestBody @Valid UpdateUserRequest request) {

		log.info("[UM_USER][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.USER_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/um/user", action = MenuPermissionAction.DELETE)
	@Audited(action = "UM_USER_DELETE", resourceType = "USER")
	public @ResponseBody ResponseEntity<ApiResponse<DeleteUserResponse>> delete(
			@RequestBody @Valid DeleteUserRequest request) {

		log.info("[UM_USER][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.USER_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/um/user", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetUserResponse>> get(@RequestBody @Valid GetUserRequest request) {

		log.info("[UM_USER][GET] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/um/user", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetUserResponse>>> gets(
			@RequestBody @Valid GetsUsersRequest request) {

		log.info("[UM_USER][GETS] page={} size={}", request.getPageNumber(), request.getPageSize());
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}

	/**
	 * Self-service profile update (first / last / phone / optional photo). Identity fields
	 * (email, provider, google id) are never changed here — see {@link IUserService#updateSelfProfile}.
	 */
	@PutMapping("/update-profile")
	public @ResponseBody ResponseEntity<ApiResponse<UpdateUserResponse>> updateSelfProfile(
			@RequestBody @Valid UpdateProfileSelfRequest request) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
			throw new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
		}
		log.info("[UM_USER][SELF_PROFILE] user={}", auth.getName());
		return ResponseEntity.ok(ApiResponse.success(service.updateSelfProfile(auth.getName(), request),
				ApiMessages.USER_UPDATED));
	}
}
