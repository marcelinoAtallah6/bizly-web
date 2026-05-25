package com.um.api.service.workflow;

import java.util.List;

import com.um.api.dto.workflow.ApproveWorkflowRequest;
import com.um.api.dto.workflow.BusinessRegistrationWorkflowRequest;
import com.um.api.dto.workflow.CreateWorkflowConfigRequest;
import com.um.api.dto.workflow.DeleteWorkflowConfigRequest;
import com.um.api.dto.workflow.UpdateWorkflowConfigRequest;
import com.um.api.dto.workflow.WorkflowCatalogScreenDto;
import com.um.api.dto.workflow.WorkflowConfigRowDto;
import com.um.api.dto.workflow.WorkflowInstanceRowDto;
import com.um.api.dto.workflow.WorkflowQueueQueryRequest;

public interface IWorkflowService {

	Long createBusinessRegistrationIfNeeded(String username, BusinessRegistrationWorkflowRequest req);

	List<WorkflowInstanceRowDto> queue(WorkflowQueueQueryRequest query);

	void approve(ApproveWorkflowRequest req);

	void reject(ApproveWorkflowRequest req);

	List<WorkflowConfigRowDto> listWorkflowConfigs(String username);

	void updateWorkflowConfig(String username, UpdateWorkflowConfigRequest req);

	List<WorkflowCatalogScreenDto> catalogScreens(String username);

	/**
	 * Permission action codes for workflow filters — union of verbs granted anywhere on
	 * {@code UM_ROLE_MENU_PERM} (same as {@code PermissionMetadataResponse#getActions()} from menu metadata).
	 */
	List<String> mutatingWorkflowActions(String username);

	void createWorkflowConfig(String username, CreateWorkflowConfigRequest req);

	void deleteWorkflowConfig(String username, DeleteWorkflowConfigRequest req);
}
