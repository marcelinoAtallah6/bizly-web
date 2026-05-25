package com.um.api.service.workflow.engine;

import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.um.api.model.workflow.WorkflowApiEndpoint;
import com.um.api.repository.workflow.WorkflowApiEndpointRepository;

/**
 * Resolves any trigger identifier to the canonical pipeline key {@code EP:{endpointId}}.
 */
@Service
public class WorkflowEngineTriggerResolver {

	public static final String TRIGGER_SOURCE_HTTP = "HTTP";
	public static final String TRIGGER_SOURCE_DOMAIN = "DOMAIN";

	@Autowired
	private WorkflowApiEndpointRepository endpointRepository;

	public String canonicalTriggerCode(Long endpointId) {
		return WorkflowEngineActionCodes.forEndpoint(endpointId);
	}

	public String normalizeTriggerCode(String actionCode) {
		if (actionCode == null || actionCode.isBlank()) {
			return actionCode;
		}
		String trimmed = actionCode.trim();
		if (WorkflowEngineActionCodes.parseEndpointId(trimmed).isPresent()) {
			return trimmed.toUpperCase(Locale.ROOT);
		}
		Optional<WorkflowApiEndpoint> byEngine = endpointRepository
				.findFirstByEngineActionCodeIgnoreCase(trimmed);
		if (byEngine.isPresent()) {
			return WorkflowEngineActionCodes.forEndpoint(byEngine.get().getId());
		}
		if (trimmed.toUpperCase(Locale.ROOT).startsWith("EP_")) {
			try {
				long id = Long.parseLong(trimmed.substring(3));
				if (endpointRepository.findById(id).filter(WorkflowApiEndpoint::isActive).isPresent()) {
					return WorkflowEngineActionCodes.forEndpoint(id);
				}
			} catch (NumberFormatException ignored) {
				// fall through
			}
		}
		return trimmed.toUpperCase(Locale.ROOT);
	}

	public boolean isKnownTrigger(String actionCode) {
		if (actionCode == null || actionCode.isBlank()) {
			return false;
		}
		if (WorkflowEngineActionCodes.parseEndpointId(actionCode).isPresent()) {
			return true;
		}
		return endpointRepository.findFirstByEngineActionCodeIgnoreCase(actionCode.trim()).isPresent();
	}

	public Optional<WorkflowApiEndpoint> resolveEndpoint(String actionCode) {
		if (actionCode == null || actionCode.isBlank()) {
			return Optional.empty();
		}
		Optional<Long> epId = WorkflowEngineActionCodes.parseEndpointId(actionCode.trim());
		if (epId.isPresent()) {
			return endpointRepository.findById(epId.get()).filter(WorkflowApiEndpoint::isActive);
		}
		return endpointRepository.findFirstByEngineActionCodeIgnoreCase(actionCode.trim())
				.filter(WorkflowApiEndpoint::isActive);
	}
}
