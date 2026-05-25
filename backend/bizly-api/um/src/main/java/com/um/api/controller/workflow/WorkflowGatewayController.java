package com.um.api.controller.workflow;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.dto.workflow.gateway.UpdateWorkflowEndpointRequest;
import com.um.api.dto.workflow.gateway.WorkflowApiEndpointAdminDto;
import com.um.api.dto.workflow.gateway.WorkflowGatewayCaptureData;
import com.um.api.dto.workflow.gateway.WorkflowGatewayCaptureRequest;
import com.um.api.dto.workflow.gateway.WorkflowGatewayMatchRequest;
import com.um.api.dto.workflow.gateway.WorkflowGatewayMatchResponse;
import com.um.api.dto.workflow.gateway.WorkflowGatewayNotifyRequest;
import com.um.api.service.security.MenuPermissionAction;
import com.um.api.service.security.RequireMenuPermission;
import com.um.api.service.workflow.WorkflowGatewayService;
import com.um.common.ApiMessages;
import com.um.common.ApiResponse;
import com.um.exception.ServiceException;

@RestController
@RequestMapping("/workflow/gateway")
public class WorkflowGatewayController {

	@Autowired
	private WorkflowGatewayService workflowGatewayService;

	@PostMapping("/match")
	public ResponseEntity<ApiResponse<WorkflowGatewayMatchResponse>> match(@RequestBody @Valid WorkflowGatewayMatchRequest req) {
		String user = requireUsername();
		return ResponseEntity.ok(ApiResponse.success(workflowGatewayService.match(user, req), ApiMessages.SUCCESS));
	}

	@PostMapping("/capture")
	public ResponseEntity<ApiResponse<WorkflowGatewayCaptureData>> capture(@RequestBody @Valid WorkflowGatewayCaptureRequest req) {
		String user = requireUsername();
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(ApiResponse.success(workflowGatewayService.capture(user, req), ""));
	}

	/** Called by API gateway after a mutating HTTP call succeeds (notification steps for EP:{id}). */
	@PostMapping("/notify-completed")
	public ResponseEntity<ApiResponse<Void>> notifyCompleted(@RequestBody @Valid WorkflowGatewayNotifyRequest req) {
		String user = requireUsername();
		workflowGatewayService.notifyCompleted(user, req.getEndpointId(), req.getBusinessId(), req.getContext());
		return ResponseEntity.ok(ApiResponse.success(null, "Triggered"));
	}

	@PostMapping("/endpoints/list")
	@RequireMenuPermission(menuRoute = "/um/workflow-config", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<List<WorkflowApiEndpointAdminDto>>> listEndpoints() {
		requireUsername();
		return ResponseEntity.ok(ApiResponse.success(workflowGatewayService.listEndpoints(), ApiMessages.SUCCESS));
	}

	@PostMapping("/endpoints/sync")
	@RequireMenuPermission(menuRoute = "/um/workflow-config", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Integer>> syncEndpoints() {
		requireUsername();
		return ResponseEntity.ok(ApiResponse.success(workflowGatewayService.syncEndpointsFromMenusAndPermissions(),
				ApiMessages.SUCCESS));
	}

	@PostMapping("/endpoints/update")
	@RequireMenuPermission(menuRoute = "/um/workflow-config", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> updateEndpoint(@RequestBody @Valid UpdateWorkflowEndpointRequest req) {
		requireUsername();
		workflowGatewayService.updateEndpoint(req);
		return ResponseEntity.ok(ApiResponse.success(null, ApiMessages.SUCCESS));
	}

	private static String requireUsername() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		if (a == null || a.getName() == null || a.getName().isBlank()) {
			throw new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
		}
		return a.getName();
	}
}
