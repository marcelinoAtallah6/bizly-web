package com.auth.api.service.registration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth.api.model.workflow.WorkflowConfigEntity;
import com.auth.api.model.workflow.WorkflowInstanceEntity;
import com.auth.api.repository.workflow.WorkflowInstanceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Creates a {@code PENDING} row in {@code UM.UM_WORKFLOW_INSTANCE} when registration approval is enabled
 * in {@code UM_WORKFLOW_CONFIG} ({@code HAS_WORKFLOW = 1}), aligned with the workflow engine.
 */
@Service
public class BusinessRegistrationWorkflowService {

	private static final Logger log = LoggerFactory.getLogger(BusinessRegistrationWorkflowService.class);
	static final String BUILTIN_BUSINESS_REGISTRATION = "BUSINESS_REGISTRATION";

	@Autowired
	private RegistrationWorkflowResolver registrationWorkflowResolver;
	@Autowired
	private WorkflowInstanceRepository workflowInstanceRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * @return workflow instance id when created, empty when skipped (no active config / already pending)
	 */
	public Optional<Long> createPendingInstanceIfNeeded(Long businessId, String businessName, Long userId,
			String username) {
		if (businessId == null || businessName == null || businessName.isBlank()) {
			return Optional.empty();
		}
		Optional<WorkflowConfigEntity> cfgOpt = registrationWorkflowResolver.resolveActiveRegistrationConfig();
		if (cfgOpt.isEmpty()) {
			log.info("[REGISTER][WORKFLOW] skipped — registration approval not enabled (HAS_WORKFLOW / engine)");
			return Optional.empty();
		}
		WorkflowConfigEntity cfg = cfgOpt.get();
		if (workflowInstanceRepository.existsByBusinessIdAndWorkflowConfigIdAndStatusIgnoreCase(businessId,
				cfg.getId(), "PENDING")) {
			log.info("[REGISTER][WORKFLOW] pending instance already exists businessId={} configId={}",
					businessId, cfg.getId());
			return workflowInstanceRepository
					.findByBusinessIdAndWorkflowConfigIdAndStatusIgnoreCase(businessId, cfg.getId(), "PENDING")
					.stream()
					.findFirst()
					.map(WorkflowInstanceEntity::getId);
		}

		WorkflowInstanceEntity inst = new WorkflowInstanceEntity();
		inst.setWorkflowConfigId(cfg.getId());
		inst.setBusinessId(businessId);
		inst.setTriggeredByUserId(userId);
		inst.setTriggeredByUsername(username);
		inst.setCurrentLevel(1);
		inst.setStatus("PENDING");
		inst.setScreenName(cfg.getScreenName());
		inst.setActionName(cfg.getActionName());
		LocalDateTime now = LocalDateTime.now();
		inst.setCreatedAt(now);
		inst.setUpdatedAt(now);
		try {
			Map<String, Object> payload = new HashMap<>();
			payload.put("businessName", businessName);
			payload.put("businessId", businessId);
			inst.setPayloadJson(objectMapper.writeValueAsString(payload));
		} catch (Exception e) {
			log.error("[REGISTER][WORKFLOW] payload serialisation failed businessId={}", businessId, e);
			return Optional.empty();
		}
		WorkflowInstanceEntity saved = workflowInstanceRepository.save(inst);
		log.info("[REGISTER][WORKFLOW] created instanceId={} businessId={} configId={} status=PENDING",
				saved.getId(), businessId, cfg.getId());
		return Optional.of(saved.getId());
	}
}
