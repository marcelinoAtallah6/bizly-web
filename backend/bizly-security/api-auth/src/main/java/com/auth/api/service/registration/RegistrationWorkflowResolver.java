package com.auth.api.service.registration;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auth.api.model.workflow.WorkflowConfigEntity;
import com.auth.api.model.workflow.WorkflowDefinitionEntity;
import com.auth.api.repository.workflow.WorkflowConfigRepository;
import com.auth.api.repository.workflow.WorkflowDefinitionRepository;

/**
 * Mirrors UM {@code WorkflowEngineRegistrationService#resolveActiveRegistrationConfig}: registration
 * is active only when {@code UM_WORKFLOW_CONFIG.HAS_WORKFLOW = 1} on the applicable row and a published
 * engine pipeline exists when imported. {@code UM_WORKFLOW_DEFINITION.STATUS} is separate from config.
 */
@Component
public class RegistrationWorkflowResolver {

	private static final int REGISTRATION_VERSION = 1;
	private static final String LEGACY_BUILTIN_KEY = BusinessRegistrationWorkflowService.BUILTIN_BUSINESS_REGISTRATION;
	private static final String ENGINE_ACTION_CODE = "BUSINESS_REGISTRATION";
	private static final String WF_ENGINE_PREFIX = "WF_ENGINE:DEF:";

	@Autowired
	private WorkflowConfigRepository configRepository;
	@Autowired
	private WorkflowDefinitionRepository definitionRepository;

	public boolean isRegistrationApprovalActive() {
		return resolveActiveRegistrationConfig().isPresent();
	}

	public Optional<WorkflowConfigEntity> resolveActiveRegistrationConfig() {
		List<WorkflowDefinitionEntity> published = definitionRepository.findPublishedGlobal(ENGINE_ACTION_CODE);
		if (!published.isEmpty()) {
			return findActiveSyncedConfig(published.get(0).getId());
		}
		if (findRegistrationDefinitionScope().isPresent()) {
			return Optional.empty();
		}
		return configRepository.findFirstByBuiltInKeyAndBusinessIdIsNull(LEGACY_BUILTIN_KEY)
				.filter(WorkflowConfigEntity::isHasWorkflow);
	}

	private Optional<WorkflowDefinitionEntity> findRegistrationDefinitionScope() {
		return definitionRepository.findFirstByActionCodeIgnoreCaseAndVersionNoAndBusinessIdIsNull(ENGINE_ACTION_CODE,
				REGISTRATION_VERSION);
	}

	private Optional<WorkflowConfigEntity> findActiveSyncedConfig(Long definitionId) {
		String prefix = WF_ENGINE_PREFIX + definitionId + ":STEP:";
		for (WorkflowConfigEntity cfg : configRepository.findByBuiltInKeyStartingWith(prefix)) {
			if (cfg.isHasWorkflow()) {
				return Optional.of(cfg);
			}
		}
		return Optional.empty();
	}
}
