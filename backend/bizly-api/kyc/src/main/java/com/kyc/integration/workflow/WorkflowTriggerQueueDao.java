package com.kyc.integration.workflow;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Enqueues workflow actions for the UM orchestrator (shared Oracle schema).
 */
@Repository
public class WorkflowTriggerQueueDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ObjectMapper objectMapper;

	public void enqueue(String actionCode, Long businessId, Map<String, Object> context) {
		try {
			String json = objectMapper.writeValueAsString(context != null ? context : Map.of());
			jdbcTemplate.update(
					"INSERT INTO UM.UM_WORKFLOW_TRIGGER_QUEUE (ID, ACTION_CODE, BUSINESS_ID, CONTEXT_JSON, STATUS, CREATED_AT) "
							+ "VALUES (UM.S_UM_WORKFLOW_TRIGGER_QUEUE.NEXTVAL, ?, ?, ?, 'PENDING', SYSTIMESTAMP)",
					actionCode, businessId, json);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to enqueue workflow trigger: " + actionCode, ex);
		}
	}
}
