package com.um.api.service.workflow.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Processes cross-service workflow triggers (e.g. KYC customer created).
 */
@Component
public class WorkflowTriggerQueueProcessor {

	private static final Logger log = LogManager.getLogger(WorkflowTriggerQueueProcessor.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private WorkflowEngineOrchestratorService orchestrator;
	@Autowired
	private WorkflowNotificationContextBuilder notificationContextBuilder;

	@Scheduled(fixedDelayString = "${um.workflow.trigger.poll-ms:2000}")
	public void pollPending() {
		List<Long> ids = jdbcTemplate.queryForList(
				"SELECT ID FROM UM.UM_WORKFLOW_TRIGGER_QUEUE WHERE STATUS = 'PENDING' ORDER BY CREATED_AT FETCH FIRST 20 ROWS ONLY",
				Long.class);
		for (Long id : ids) {
			processOne(id);
		}
	}

	public void processOne(Long id) {
		int claimed = jdbcTemplate.update(
				"UPDATE UM.UM_WORKFLOW_TRIGGER_QUEUE SET STATUS = 'PROCESSING' WHERE ID = ? AND STATUS = 'PENDING'",
				id);
		if (claimed == 0) {
			return;
		}
		try {
			Map<String, Object> row = jdbcTemplate.queryForMap(
					"SELECT ACTION_CODE, BUSINESS_ID, CONTEXT_JSON FROM UM.UM_WORKFLOW_TRIGGER_QUEUE WHERE ID = ?",
					id);
			String actionCode = (String) row.get("ACTION_CODE");
			Long businessId = row.get("BUSINESS_ID") != null ? ((Number) row.get("BUSINESS_ID")).longValue() : null;
			String json = (String) row.get("CONTEXT_JSON");
			Map<String, Object> ctx = json != null
					? objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {})
					: new HashMap<>();
			orchestrator.onActionCompleted(actionCode, businessId,
					notificationContextBuilder.enrich(ctx, businessId));
			jdbcTemplate.update(
					"UPDATE UM.UM_WORKFLOW_TRIGGER_QUEUE SET STATUS = 'DONE', PROCESSED_AT = SYSTIMESTAMP WHERE ID = ?",
					id);
			log.info("[WF_TRIGGER] processed id={} action={} businessId={}", id, actionCode, businessId);
		} catch (Exception ex) {
			log.error("[WF_TRIGGER] failed id={}: {}", id, ex.toString(), ex);
			jdbcTemplate.update(
					"UPDATE UM.UM_WORKFLOW_TRIGGER_QUEUE SET STATUS = 'FAILED', LAST_ERROR = ?, PROCESSED_AT = SYSTIMESTAMP WHERE ID = ?",
					truncate(ex.getMessage(), 3900), id);
		}
	}

	private static String truncate(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.length() <= max ? s : s.substring(0, max);
	}
}
