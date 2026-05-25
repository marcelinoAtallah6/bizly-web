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

import com.um.api.dto.workflow.ApproveWorkflowRequest;
import com.um.api.dto.workflow.BusinessRegistrationWorkflowRequest;
import com.um.api.dto.workflow.CreateWorkflowConfigRequest;
import com.um.api.dto.workflow.DeleteWorkflowConfigRequest;
import com.um.api.dto.workflow.UpdateWorkflowConfigRequest;
import com.um.api.dto.workflow.WorkflowCatalogScreenDto;
import com.um.api.dto.workflow.WorkflowConfigRowDto;
import com.um.api.dto.workflow.WorkflowEndpointCatalogRowDto;
import com.um.api.dto.workflow.WorkflowInstanceRowDto;
import com.um.api.dto.workflow.WorkflowQueueQueryRequest;
import com.um.api.service.security.MenuPermissionAction;
import com.um.api.service.security.RequireMenuPermission;
import com.um.api.service.workflow.IWorkflowService;
import com.um.api.service.workflow.WorkflowGatewayService;
import com.um.common.ApiMessages;
import com.um.common.ApiResponse;
import com.um.exception.ServiceException;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

	@Autowired
	private IWorkflowService workflowService;
	@Autowired
	private WorkflowGatewayService workflowGatewayService;

	@PostMapping("/instance/business-registration")
	public ResponseEntity<ApiResponse<Long>> businessRegistration(@RequestBody @Valid BusinessRegistrationWorkflowRequest req) {
		String user = requireUsername();
		Long id = workflowService.createBusinessRegistrationIfNeeded(user, req);
		return ResponseEntity.ok(ApiResponse.success(id, id == null ? "No workflow configured" : "Workflow created"));
	}

	@PostMapping("/instance/queue")
	@RequireMenuPermission(menuRoute = "/um/workflow-queue", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<List<WorkflowInstanceRowDto>>> queue(@RequestBody(required = false) WorkflowQueueQueryRequest query) {
		if (query == null) {
			query = new WorkflowQueueQueryRequest();
		}
		requireUsername();
		return ResponseEntity.ok(ApiResponse.success(workflowService.queue(query), ApiMessages.SUCCESS));
	}

	@PostMapping("/instance/approve")
	public ResponseEntity<ApiResponse<Void>> approve(@RequestBody @Valid ApproveWorkflowRequest req) {
		requireUsername();
		workflowService.approve(req);
		return ResponseEntity.ok(ApiResponse.success(null, "Approved"));
	}

	@PostMapping("/instance/reject")
	public ResponseEntity<ApiResponse<Void>> reject(@RequestBody @Valid ApproveWorkflowRequest req) {
		requireUsername();
		workflowService.reject(req);
		return ResponseEntity.ok(ApiResponse.success(null, "Rejected"));
	}

	@PostMapping("/catalog/mutating-actions")
	public ResponseEntity<ApiResponse<List<String>>> mutatingActions() {
		String user = requireUsername();
		return ResponseEntity.ok(ApiResponse.success(workflowService.mutatingWorkflowActions(user), ApiMessages.SUCCESS));
	}

	@PostMapping("/config/list")
	@RequireMenuPermission(menuRoute = "/um/workflow-config", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<List<WorkflowConfigRowDto>>> listConfigs() {
		String user = requireUsername();
		return ResponseEntity.ok(ApiResponse.success(workflowService.listWorkflowConfigs(user), ApiMessages.SUCCESS));
	}

	@PostMapping("/config/update")
	@RequireMenuPermission(menuRoute = "/um/workflow-config", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> updateConfig(@RequestBody @Valid UpdateWorkflowConfigRequest req) {
		String user = requireUsername();
		workflowService.updateWorkflowConfig(user, req);
		return ResponseEntity.ok(ApiResponse.success(null, ApiMessages.SUCCESS));
	}

	@PostMapping("/config/endpoint-catalog")
	@RequireMenuPermission(menuRoute = "/um/workflow-config", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<List<WorkflowEndpointCatalogRowDto>>> endpointCatalog() {
		String user = requireUsername();
		return ResponseEntity
				.ok(ApiResponse.success(workflowGatewayService.mutatingEndpointCatalogForWorkflowForm(user), ApiMessages.SUCCESS));
	}

	@PostMapping("/config/catalog")
	public ResponseEntity<ApiResponse<List<WorkflowCatalogScreenDto>>> catalog() {
		String user = requireUsername();
		return ResponseEntity.ok(ApiResponse.success(workflowService.catalogScreens(user), ApiMessages.SUCCESS));
	}

	@PostMapping("/config/create")
	@RequireMenuPermission(menuRoute = "/um/workflow-config", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> createConfig(@RequestBody @Valid CreateWorkflowConfigRequest req) {
		String user = requireUsername();
		workflowService.createWorkflowConfig(user, req);
		return ResponseEntity.ok(ApiResponse.success(null, ApiMessages.SUCCESS));
	}

	@PostMapping("/config/delete")
	@RequireMenuPermission(menuRoute = "/um/workflow-config", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> deleteConfig(@RequestBody @Valid DeleteWorkflowConfigRequest req) {
		String user = requireUsername();
		workflowService.deleteWorkflowConfig(user, req);
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
