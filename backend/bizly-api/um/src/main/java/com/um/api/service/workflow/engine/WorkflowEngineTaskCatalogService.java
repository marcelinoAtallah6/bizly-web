package com.um.api.service.workflow.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.um.api.dto.workflow.WorkflowEndpointCatalogRowDto;
import com.um.api.dto.workflow.engine.WorkflowTaskCatalogCategoryDto;
import com.um.api.dto.workflow.engine.WorkflowTaskCatalogItemDto;
import com.um.api.service.workflow.WorkflowGatewayService;

@Service
public class WorkflowEngineTaskCatalogService {

	@Autowired
	private WorkflowGatewayService workflowGatewayService;

	public List<WorkflowTaskCatalogCategoryDto> listCatalog(String username) {
		Map<String, WorkflowTaskCatalogCategoryDto> byKey = new LinkedHashMap<>();
		addCategory(byKey, "QUICK_ADD", "Quick Add");
		addCategory(byKey, "APPROVAL", "Approval");
		addCategory(byKey, "NOTIFICATION", "Notification");
		addCategory(byKey, "HTTP", "HTTP");
		addCategory(byKey, "SYSTEM", "System");
		addCategory(byKey, "VARIABLE", "Variable");
		addCategory(byKey, "AGENTIC", "Agentic");

		item(byKey, "QUICK_ADD", "APPROVAL_MANUAL", "Manual Approval", WorkflowEngineOrchestratorService.STEP_APPROVAL,
				"shield-check", "Route an action through manual approval.");
		item(byKey, "APPROVAL", "APPROVAL_MANUAL", "Manual Approval", WorkflowEngineOrchestratorService.STEP_APPROVAL,
				"shield-check", "Single-level or custom approval.");
		item(byKey, "APPROVAL", "APPROVAL_MAKER_CHECKER", "Maker / Checker",
				WorkflowEngineOrchestratorService.STEP_APPROVAL, "users", "Maker initiates; checker tiers approve.");
		item(byKey, "APPROVAL", "APPROVAL_SCREENING", "Screening Review",
				WorkflowEngineOrchestratorService.STEP_APPROVAL, "search", "Compliance or screening review step.");

		item(byKey, "QUICK_ADD", "NOTIF_EMAIL", "Send Email", WorkflowEngineOrchestratorService.STEP_NOTIFICATION,
				"mail", "Email via template outbox.");
		item(byKey, "NOTIFICATION", "NOTIF_EMAIL", "Send Email", WorkflowEngineOrchestratorService.STEP_NOTIFICATION,
				"mail", "Deliver email using NOTIF_EMAIL_TEMPLATE.");
		item(byKey, "NOTIFICATION", "NOTIF_INBOX", "Send Inbox Notification",
				WorkflowEngineOrchestratorService.STEP_NOTIFICATION, "inbox", "Feeds the notification box tracker.");
		item(byKey, "NOTIFICATION", "NOTIF_BOTH", "Email + Inbox",
				WorkflowEngineOrchestratorService.STEP_NOTIFICATION, "bell", "Both EMAIL and INBOX channels.");

		item(byKey, "HTTP", "HTTP_TASK", "HTTP Task", "HTTP_TASK", "world", "Single REST call.");
		item(byKey, "HTTP", "HTTP_POLL", "HTTP Poll", "HTTP_POLL", "refresh", "Poll until condition.");
		item(byKey, "HTTP", "HTTP_REST", "REST Call", "HTTP_TASK", "api", "Maps to registered API endpoints.");

		item(byKey, "SYSTEM", "SYS_SWITCH", "Switch", "SYSTEM", "git-branch", "Conditional branch (stored config).");
		item(byKey, "SYSTEM", "SYS_FORK_JOIN", "Fork Join", "SYSTEM", "git-merge", "Parallel fork/join.");
		item(byKey, "SYSTEM", "SYS_DO_WHILE", "Do While", "SYSTEM", "repeat", "Loop until condition.");

		item(byKey, "VARIABLE", "VAR_SET", "Set Variable", "SYSTEM", "variable", "Set pipeline variable.");
		item(byKey, "VARIABLE", "VAR_WAIT", "Wait", "SYSTEM", "clock", "Delay execution.");

		item(byKey, "AGENTIC", "AGENT_CHAT", "Chat Complete", "SYSTEM", "message", "LLM chat step (placeholder).");
		item(byKey, "AGENTIC", "AGENT_EMBED", "Generate Embedding", "SYSTEM", "vector", "Embedding step (placeholder).");

		List<WorkflowEndpointCatalogRowDto> endpoints = workflowGatewayService
				.mutatingEndpointCatalogForWorkflowForm(username);
		for (WorkflowEndpointCatalogRowDto ep : endpoints) {
			WorkflowTaskCatalogItemDto dto = new WorkflowTaskCatalogItemDto();
			dto.setTaskKey("HTTP_EP_" + ep.getEndpointId());
			dto.setTitle(ep.getDisplayLabel() != null ? ep.getDisplayLabel() : ep.getPathAntPattern());
			dto.setDescription(ep.getHttpMethod() + " " + ep.getPathAntPattern());
			dto.setStepType("HTTP_TASK");
			dto.setCategoryKey("HTTP");
			dto.setIconKey("api");
			dto.setEndpointPath(ep.getPathAntPattern());
			dto.setHttpMethod(ep.getHttpMethod());
			byKey.get("HTTP").getItems().add(dto);
		}

		return new ArrayList<>(byKey.values());
	}

	private static void addCategory(Map<String, WorkflowTaskCatalogCategoryDto> byKey, String key, String label) {
		WorkflowTaskCatalogCategoryDto cat = new WorkflowTaskCatalogCategoryDto();
		cat.setCategoryKey(key);
		cat.setCategoryLabel(label);
		byKey.put(key, cat);
	}

	private static void item(Map<String, WorkflowTaskCatalogCategoryDto> byKey, String catKey, String taskKey,
			String title, String stepType, String icon, String desc) {
		WorkflowTaskCatalogItemDto dto = new WorkflowTaskCatalogItemDto();
		dto.setTaskKey(taskKey);
		dto.setTitle(title);
		dto.setStepType(stepType);
		dto.setCategoryKey(catKey);
		dto.setIconKey(icon);
		dto.setDescription(desc);
		byKey.get(catKey).getItems().add(dto);
	}
}
