package com.broadcast.api.controller.broadcast;

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

import com.broadcast.api.dto.broadcast.CreateBroadcastRequest;
import com.broadcast.api.dto.broadcast.CreateBroadcastResponse;
import com.broadcast.api.dto.broadcast.GetBroadcastRequest;
import com.broadcast.api.dto.broadcast.GetBroadcastResponse;
import com.broadcast.api.dto.broadcast.GetsBroadcastsRequest;
import com.broadcast.api.dto.broadcast.PreviewBroadcastRequest;
import com.broadcast.api.dto.broadcast.PreviewBroadcastResponse;
import com.broadcast.api.dto.broadcast.SendBroadcastRequest;
import com.broadcast.api.dto.broadcast.SendBroadcastResponse;
import com.broadcast.api.service.broadcast.IBroadcastService;
import com.broadcast.audit.Audited;
import com.broadcast.common.ApiMessages;
import com.broadcast.common.ApiResponse;
import com.broadcast.common.PageResponse;
import com.broadcast.security.MenuPermissionAction;
import com.broadcast.security.RequireMenuPermission;

@RestController
@RequestMapping("/message")
public class BroadcastMessageController {

	private static final Logger log = LogManager.getLogger(BroadcastMessageController.class);

	@Autowired
	private IBroadcastService broadcastService;

	@PostMapping("/create")
	@Audited(action = "UM_BROADCAST_CREATE", resourceType = "BROADCAST_MESSAGE")
	@RequireMenuPermission(menuRoute = "/broadcast", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<CreateBroadcastResponse>> create(
			@RequestBody @Valid CreateBroadcastRequest request, HttpServletRequest httpRequest) {

		String user = httpRequest.getHeader("X-User");
		log.info("[BCAST][API][CREATE] targetType={} queueForSend={}", request.getTargetType(), request.isQueueForSend());

		return ResponseEntity
				.ok(ApiResponse.success(broadcastService.create(request, user), ApiMessages.BROADCAST_CREATED));
	}

	@PostMapping("/preview")
	@Audited(action = "UM_BROADCAST_PREVIEW", resourceType = "BROADCAST_MESSAGE")
	@RequireMenuPermission(menuRoute = "/broadcast", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PreviewBroadcastResponse>> preview(
			@RequestBody PreviewBroadcastRequest request) {

		log.info("[BCAST][API][PREVIEW]");
		return ResponseEntity.ok(ApiResponse.success(broadcastService.preview(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/send")
	@Audited(action = "UM_BROADCAST_SEND", resourceType = "BROADCAST_MESSAGE")
	@RequireMenuPermission(menuRoute = "/broadcast", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<SendBroadcastResponse>> send(
			@RequestBody @Valid SendBroadcastRequest request) {

		log.info("[BCAST][API][SEND] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(broadcastService.send(request), ApiMessages.BROADCAST_QUEUED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/broadcast", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetBroadcastResponse>> get(
			@RequestBody @Valid GetBroadcastRequest request) {

		log.info("[BCAST][API][GET] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(broadcastService.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/broadcast", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetBroadcastResponse>>> gets(
			@RequestBody @Valid GetsBroadcastsRequest request) {

		log.info("[BCAST][API][GETS] page={} size={}", request.getPageNumber(), request.getPageSize());
		return ResponseEntity.ok(ApiResponse.success(broadcastService.gets(request), ApiMessages.SUCCESS));
	}
}
