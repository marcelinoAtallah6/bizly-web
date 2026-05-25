package com.um.api.service.workflow.engine;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.um.api.model.workflow.engine.WorkflowDefinition;
import com.um.api.model.workflow.engine.WorkflowStep;
import com.um.api.repository.workflow.engine.WorkflowDefinitionRepository;
import com.um.api.repository.workflow.engine.WorkflowStepRepository;

/**
 * HTTP gateway hooks: resolve published engine pipelines bound to {@code EP:{endpointId}}.
 */
@Service
public class WorkflowEngineHttpNotificationService {

	@Autowired
	private WorkflowDefinitionRepository definitionRepository;
	@Autowired
	private WorkflowStepRepository stepRepository;

	public Optional<WorkflowDefinition> findPublishedPipelineForEndpoint(long endpointId, Long businessId) {
		return definitionRepository.findBestPublished(WorkflowEngineActionCodes.forEndpoint(endpointId), businessId);
	}

	public boolean hasPublishedPipelineForEndpoint(long endpointId, Long businessId) {
		return findPublishedPipelineForEndpoint(endpointId, businessId).isPresent();
	}

	public boolean hasActiveApprovalStepForEndpoint(long endpointId, Long businessId) {
		Optional<WorkflowDefinition> def = findPublishedPipelineForEndpoint(endpointId, businessId);
		if (!def.isPresent()) {
			return false;
		}
		List<WorkflowStep> steps = stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.get().getId(), 1);
		for (WorkflowStep step : steps) {
			if (WorkflowEngineOrchestratorService.STEP_APPROVAL.equalsIgnoreCase(step.getStepType())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasActiveNotificationStepForEndpoint(long endpointId, Long businessId) {
		Optional<WorkflowDefinition> def = findPublishedPipelineForEndpoint(endpointId, businessId);
		if (!def.isPresent()) {
			return false;
		}
		List<WorkflowStep> steps = stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.get().getId(), 1);
		for (WorkflowStep step : steps) {
			if (WorkflowEngineOrchestratorService.STEP_NOTIFICATION.equalsIgnoreCase(step.getStepType())) {
				return true;
			}
		}
		return false;
	}

}
