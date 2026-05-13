package com.bm.api.controller.serviceitem;

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

import com.bm.api.dto.serviceitem.add.AddServiceItemRequest;
import com.bm.api.dto.serviceitem.add.AddServiceItemResponse;
import com.bm.api.dto.serviceitem.delete.DeactivateServiceItemRequest;
import com.bm.api.dto.serviceitem.delete.DeactivateServiceItemResponse;
import com.bm.api.dto.serviceitem.get.GetServiceItemRequest;
import com.bm.api.dto.serviceitem.get.GetServiceItemResponse;
import com.bm.api.dto.serviceitem.gets.GetsServiceItemsRequest;
import com.bm.api.dto.serviceitem.update.UpdateServiceItemRequest;
import com.bm.api.dto.serviceitem.update.UpdateServiceItemResponse;
import com.bm.api.service.serviceitem.IServiceItemService;
import com.bm.audit.Audited;
import com.bm.common.ApiMessages;
import com.bm.common.ApiResponse;
import com.bm.common.PageResponse;
import com.bm.security.MenuPermissionAction;
import com.bm.security.RequireMenuPermission;

@RestController
@RequestMapping("/service-item")
public class ServiceItemController {

	private static final Logger log = LogManager.getLogger(ServiceItemController.class);

	@Autowired
	private IServiceItemService service;

	@PostMapping("/add")
	@Audited(action = "BM_SERVICE_ITEM_ADD", resourceType = "SERVICE_ITEM")
	@RequireMenuPermission(menuRoute = "/bm/services", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddServiceItemResponse>> add(HttpServletRequest httpRequest,
			@RequestBody @Valid AddServiceItemRequest request) {

		log.info("[BM_SERVICE_ITEM][ADD] name={}", request.getName());
		String user = httpRequest.getHeader("X-User");
		return ResponseEntity.ok(ApiResponse.success(service.add(request, user), ApiMessages.SERVICE_ITEM_ADDED));
	}

	@PostMapping("/update")
	@Audited(action = "BM_SERVICE_ITEM_UPDATE", resourceType = "SERVICE_ITEM")
	@RequireMenuPermission(menuRoute = "/bm/services", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateServiceItemResponse>> update(HttpServletRequest httpRequest,
			@RequestBody @Valid UpdateServiceItemRequest request) {

		log.info("[BM_SERVICE_ITEM][UPDATE] id={}", request.getId());
		String user = httpRequest.getHeader("X-User");
		return ResponseEntity
				.ok(ApiResponse.success(service.update(request, user), ApiMessages.SERVICE_ITEM_UPDATED));
	}

	@PostMapping("/deactivate")
	@Audited(action = "BM_SERVICE_ITEM_DEACTIVATE", resourceType = "SERVICE_ITEM")
	@RequireMenuPermission(menuRoute = "/bm/services", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeactivateServiceItemResponse>> deactivate(
			HttpServletRequest httpRequest, @RequestBody @Valid DeactivateServiceItemRequest request) {

		log.info("[BM_SERVICE_ITEM][DEACTIVATE] id={}", request.getId());
		String user = httpRequest.getHeader("X-User");
		return ResponseEntity.ok(
				ApiResponse.success(service.deactivate(request, user), ApiMessages.SERVICE_ITEM_DEACTIVATED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/bm/services", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetServiceItemResponse>> get(
			@RequestBody @Valid GetServiceItemRequest request) {

		log.info("[BM_SERVICE_ITEM][GET] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/bm/services", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetServiceItemResponse>>> gets(
			@RequestBody @Valid GetsServiceItemsRequest request) {

		log.info("[BM_SERVICE_ITEM][GETS] page={}", request.getPageNumber());
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
