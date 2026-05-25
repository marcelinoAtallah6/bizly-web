package com.um.api.service.workflow.engine;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Runs {@code TRIGGER_KIND = SCHEDULED} workflow pipelines (e.g. birthday emails) without a user HTTP action.
 * Candidates are resolved here; {@link WorkflowEngineOrchestratorService} enqueues {@code UM_NOTIF_OUTBOX} rows.
 */
@Service
public class WorkflowEngineScheduledTriggerService {

	private static final Logger log = LogManager.getLogger(WorkflowEngineScheduledTriggerService.class);

	public static final String ACTION_BIRTHDAY_USER = "BIRTHDAY_USER_SUCCESS";
	public static final String ACTION_BIRTHDAY_CUSTOMER = "BIRTHDAY_CUSTOMER_SUCCESS";

	@Autowired
	private WorkflowEngineOrchestratorService orchestrator;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${workflow.engine.scheduled.birthday-promo-code:BDAY-PROMO}")
	private String birthdayPromoCode;

	@Scheduled(cron = "${workflow.engine.scheduled.birthday-cron:0 0 7 * * *}")
	public void runDailyScheduledTriggers() {
		try {
			int users = runUserBirthdays();
			int customers = runCustomerBirthdays();
			if (users > 0 || customers > 0) {
				log.info("[WF_ENGINE][SCHEDULED] birthday triggers users={} customers={}", users, customers);
			}
		} catch (Exception ex) {
			log.error("[WF_ENGINE][SCHEDULED] daily run failed: {}", ex.toString(), ex);
		}
	}

	private int runUserBirthdays() {
		String sql = "SELECT u.ID, u.BUSINESS_ID, u.EMAIL, u.USERNAME, u.FIRST_NAME, u.LAST_NAME "
				+ "FROM UM.UM_USER u WHERE u.EMAIL IS NOT NULL AND u.DATE_OF_BIRTH IS NOT NULL "
				+ "AND EXTRACT(MONTH FROM u.DATE_OF_BIRTH) = EXTRACT(MONTH FROM CAST(SYSDATE AS DATE)) "
				+ "AND EXTRACT(DAY FROM u.DATE_OF_BIRTH) = EXTRACT(DAY FROM CAST(SYSDATE AS DATE))";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		int fired = 0;
		for (Map<String, Object> row : rows) {
			Map<String, Object> ctx = new HashMap<>();
			ctx.put("userId", numberVal(row.get("ID")));
			ctx.put("businessId", numberVal(row.get("BUSINESS_ID")));
			ctx.put("email", stringVal(row.get("EMAIL")));
			ctx.put("username", stringVal(row.get("USERNAME")));
			ctx.put("firstName", stringVal(row.get("FIRST_NAME")));
			ctx.put("lastName", stringVal(row.get("LAST_NAME")));
			ctx.put("promoCode", birthdayPromoCode);
			ctx.put("date", LocalDate.now().toString());
			Long businessId = longVal(ctx.get("businessId"));
			orchestrator.onActionCompleted(ACTION_BIRTHDAY_USER, businessId, ctx);
			fired++;
		}
		return fired;
	}

	private int runCustomerBirthdays() {
		String sql = "SELECT c.ID, c.BUSINESS_ID, c.EMAIL, c.FIRST_NAME, c.LAST_NAME, c.FULL_NAME "
				+ "FROM UM.KYC_CUSTOMER c WHERE c.EMAIL IS NOT NULL AND c.DOB IS NOT NULL "
				+ "AND EXTRACT(MONTH FROM c.DOB) = EXTRACT(MONTH FROM CAST(SYSDATE AS DATE)) "
				+ "AND EXTRACT(DAY FROM c.DOB) = EXTRACT(DAY FROM CAST(SYSDATE AS DATE))";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		int fired = 0;
		for (Map<String, Object> row : rows) {
			String fn = stringVal(row.get("FIRST_NAME"));
			String ln = stringVal(row.get("LAST_NAME"));
			String full = stringVal(row.get("FULL_NAME"));
			String display = full != null && !full.isBlank() ? full : ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
			Map<String, Object> ctx = new HashMap<>();
			ctx.put("customerId", numberVal(row.get("ID")));
			ctx.put("businessId", numberVal(row.get("BUSINESS_ID")));
			ctx.put("email", stringVal(row.get("EMAIL")));
			ctx.put("firstName", fn);
			ctx.put("lastName", ln);
			ctx.put("name", display);
			ctx.put("promoCode", birthdayPromoCode);
			ctx.put("date", LocalDate.now().toString());
			Long businessId = longVal(ctx.get("businessId"));
			orchestrator.onActionCompleted(ACTION_BIRTHDAY_CUSTOMER, businessId, ctx);
			fired++;
		}
		return fired;
	}

	private static String stringVal(Object o) {
		return o == null ? null : o.toString().trim();
	}

	private static Long numberVal(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Number) {
			return ((Number) o).longValue();
		}
		try {
			return Long.parseLong(o.toString());
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static Long longVal(Object o) {
		return o instanceof Long ? (Long) o : numberVal(o);
	}
}
