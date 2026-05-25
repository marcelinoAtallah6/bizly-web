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

import com.um.api.dto.workflow.engine.CreateWorkflowDefinitionRequest;
import com.um.api.dto.workflow.engine.EmailTemplateKeyDto;
import com.um.api.dto.workflow.engine.GetWorkflowDefinitionRequest;
import com.um.api.dto.workflow.engine.SaveWorkflowStepsRequest;
import com.um.api.dto.workflow.engine.OnEndpointCompletedRequest;
import com.um.api.dto.workflow.engine.TriggerWorkflowActionRequest;
import com.um.api.service.workflow.engine.WorkflowEngineActionCodes;
import com.um.api.dto.workflow.engine.UpdateWorkflowDefinitionRequest;
import com.um.api.dto.workflow.engine.WorkflowActionDto;
import com.um.api.dto.workflow.engine.WorkflowDefinitionDetailDto;
import com.um.api.dto.workflow.engine.WorkflowDefinitionIdRequest;
import com.um.api.dto.workflow.engine.WorkflowDefinitionRowDto;
import com.um.api.service.security.MenuPermissionAction;
import com.um.api.service.security.RequireMenuPermission;
import com.um.api.dto.workflow.engine.ApplicationActionTargetDto;
import com.um.api.dto.workflow.engine.WorkflowTaskCatalogCategoryDto;
import com.um.api.model.workflow.engine.WorkflowDefinition;
import com.um.api.repository.workflow.engine.WorkflowDefinitionRepository;
import com.um.api.service.workflow.WorkflowGatewayService;
import com.um.api.service.workflow.engine.WorkflowEngineAdminService;
import com.um.api.service.workflow.engine.WorkflowEngineOrchestratorService;
import com.um.api.service.workflow.engine.WorkflowEngineRoleAccessService;
import com.um.api.service.workflow.engine.WorkflowEngineScheduledTriggerService;
import com.um.api.service.workflow.engine.WorkflowEngineTriggerResolver;
import com.um.api.service.workflow.engine.WorkflowNotificationContextBuilder;
import com.um.api.service.workflow.engine.WorkflowEngineTaskCatalogService;
import com.um.common.ApiMessages;
import com.um.common.ApiResponse;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@RestController
@RequestMapping("/workflow/engine")
public class WorkflowEngineController {

	@Autowired
	private WorkflowEngineOrchestratorService orchestrator;
	@Autowired
	private WorkflowEngineAdminService adminService;
	@Autowired
	private WorkflowEngineTaskCatalogService taskCatalogService;
	@Autowired
	private WorkflowEngineRoleAccessService roleAccessService;
	@Autowired
	private WorkflowDefinitionRepository definitionRepository;
	@Autowired
	private WorkflowNotificationContextBuilder notificationContextBuilder;
	@Autowired
	private WorkflowEngineTriggerResolver triggerResolver;
	@Autowired
	private WorkflowEngineScheduledTriggerService scheduledTriggerService;
	@Autowired
	private WorkflowGatewayService workflowGatewayService;

	@PostMapping("/actions/list")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<List<WorkflowActionDto>>> listActions() {
		requireUsername();
		return ResponseEntity.ok(ApiResponse.success(adminService.listActions(), ApiMessages.SUCCESS));
	}

	@PostMapping("/templates/list")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<List<EmailTemplateKeyDto>>> listTemplates() {
		requireUsername();
		return ResponseEntity.ok(ApiResponse.success(adminService.listEmailTemplates(), ApiMessages.SUCCESS));
	}

	@PostMapping("/tasks/catalog")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<List<WorkflowTaskCatalogCategoryDto>>> taskCatalog() {
		String user = requireUsername();
		return ResponseEntity.ok(ApiResponse.success(taskCatalogService.listCatalog(user), ApiMessages.SUCCESS));
	}

	@PostMapping("/application-actions/list")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<List<ApplicationActionTargetDto>>> applicationActions() {
		requireUsername();
		return ResponseEntity.ok(
				ApiResponse.success(adminService.listApplicationActionTargets(), ApiMessages.SUCCESS));
	}

	@PostMapping("/definitions/list")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<List<WorkflowDefinitionRowDto>>> listDefinitions() {
		requireUsername();
		return ResponseEntity.ok(ApiResponse.success(adminService.listDefinitions(), ApiMessages.SUCCESS));
	}

	@PostMapping("/definitions/get")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.VIEW)
	public ResponseEntity<ApiResponse<WorkflowDefinitionDetailDto>> getDefinition(
			@RequestBody @Valid GetWorkflowDefinitionRequest req) {
		requireUsername();
		return ResponseEntity.ok(ApiResponse.success(adminService.getDefinition(req.getId()), ApiMessages.SUCCESS));
	}

	@PostMapping("/definitions/create")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Long>> createDefinition(@RequestBody @Valid CreateWorkflowDefinitionRequest req) {
		String user = requireUsername();
		Long id = adminService.createDefinition(user, req);
		return ResponseEntity.ok(ApiResponse.success(id, ApiMessages.SUCCESS));
	}

	@PostMapping("/definitions/update")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> updateDefinition(@RequestBody @Valid UpdateWorkflowDefinitionRequest req) {
		String user = requireUsername();
		adminService.updateDefinition(user, req);
		return ResponseEntity.ok(ApiResponse.success(null, ApiMessages.SUCCESS));
	}

	@PostMapping("/definitions/save-steps")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> saveSteps(@RequestBody @Valid SaveWorkflowStepsRequest req) {
		String user = requireUsername();
		adminService.saveSteps(user, req);
		return ResponseEntity.ok(ApiResponse.success(null, ApiMessages.SUCCESS));
	}

	@PostMapping("/definitions/publish")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> publish(@RequestBody @Valid WorkflowDefinitionIdRequest req) {
		String user = requireUsername();
		adminService.publish(user, req.getId());
		return ResponseEntity.ok(ApiResponse.success(null, "Published"));
	}

	@PostMapping("/definitions/disable")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> disable(@RequestBody @Valid WorkflowDefinitionIdRequest req) {
		String user = requireUsername();
		adminService.disable(user, req.getId());
		return ResponseEntity.ok(ApiResponse.success(null, "Disabled"));
	}

	@PostMapping("/definitions/delete")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> delete(@RequestBody @Valid WorkflowDefinitionIdRequest req) {
		requireUsername();
		adminService.deleteDraft(req.getId());
		return ResponseEntity.ok(ApiResponse.success(null, ApiMessages.SUCCESS));
	}

	@PostMapping("/on-endpoint-completed")
	public ResponseEntity<ApiResponse<Void>> onEndpointCompleted(@RequestBody @Valid OnEndpointCompletedRequest req) {
		String user = requireUsername();
		Long businessId = req.getBusinessId() != null ? req.getBusinessId() : BusinessContextHolder.currentBusinessId();
		workflowGatewayService.notifyCompleted(user, req.getEndpointId(), businessId, req.getContext());
		return ResponseEntity.ok(ApiResponse.success(null, "Triggered"));
	}

	@PostMapping("/trigger")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> trigger(@RequestBody @Valid TriggerWorkflowActionRequest req) {
		String user = requireUsername();
		Long businessId = req.getBusinessId() != null ? req.getBusinessId() : BusinessContextHolder.currentBusinessId();
		String actionCode = triggerResolver.normalizeTriggerCode(req.getActionCode().trim());
		WorkflowDefinition def = definitionRepository.findBestPublished(actionCode, businessId).orElse(null);
		if (def != null && !roleAccessService.canRunPipeline(def, user)) {
			throw new ServiceException("You do not have permission to run this workflow.", HttpStatus.FORBIDDEN);
		}
		java.util.Map<String, Object> ctx = req.getContext() != null
				? new java.util.HashMap<>(req.getContext())
				: new java.util.HashMap<>();
		ctx.putIfAbsent("username", user);
		orchestrator.onActionCompleted(actionCode, businessId, notificationContextBuilder.enrich(ctx, businessId));
		return ResponseEntity.ok(ApiResponse.success(null, "Triggered"));
	}

	/** Manual run of scheduled birthday pipelines (same as daily cron). */
	@PostMapping("/scheduled/run-birthdays")
	@RequireMenuPermission(menuRoute = "/um/workflow-engine", action = MenuPermissionAction.EDIT)
	public ResponseEntity<ApiResponse<Void>> runBirthdayScheduled() {
		requireUsername();
		scheduledTriggerService.runDailyScheduledTriggers();
		return ResponseEntity.ok(ApiResponse.success(null, "Birthday job executed"));
	}

	private static String requireUsername() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		if (a == null || a.getName() == null || a.getName().isBlank()) {
			throw new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
		}
		return a.getName();
	}
}
