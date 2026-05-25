package com.um.api.service.workflow.engine;



import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.Collections;

import java.util.HashMap;

import java.util.LinkedHashSet;

import java.util.List;

import java.util.Locale;

import java.util.Map;

import java.util.Optional;

import java.util.Set;



import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;

import org.springframework.transaction.annotation.Transactional;



import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.um.api.model.user.User;

import com.um.api.model.workflow.engine.NotifOutbox;

import com.um.api.model.workflow.engine.WorkflowDefinition;

import com.um.api.model.workflow.engine.WorkflowStep;

import com.um.api.repository.user.UserRepository;

import com.um.api.repository.workflow.engine.NotifOutboxRepository;

import com.um.api.repository.workflow.engine.WorkflowDefinitionRepository;

import com.um.api.repository.workflow.engine.WorkflowStepRepository;



/**

 * Runs published workflow pipelines after a business action completes.

 * Approval steps are enforced separately by the gateway; this service handles

 * post-success steps such as {@code NOTIFICATION}.

 */

@Service

public class WorkflowEngineOrchestratorService {



	private static final Logger log = LogManager.getLogger(WorkflowEngineOrchestratorService.class);



	public static final String STEP_NOTIFICATION = "NOTIFICATION";

	public static final String STEP_APPROVAL = "APPROVAL";



	public static final String ACTION_USER_CREATED = "USER_CREATED_SUCCESS";

	public static final String ACTION_CUSTOMER_CREATED = "CUSTOMER_CREATED_SUCCESS";



	public static final String STATUS_PENDING = "PENDING";

	public static final String CHANNEL_EMAIL = "EMAIL";

	public static final String CHANNEL_INBOX = "INBOX";

	public static final String CHANNEL_BOTH = "BOTH";



	private static final String TIMING_ON_ACTION_SUCCESS = "ON_ACTION_SUCCESS";

	private static final String TIMING_AFTER_APPROVAL = "AFTER_APPROVAL";



	@Autowired

	private WorkflowDefinitionRepository definitionRepository;

	@Autowired

	private WorkflowStepRepository stepRepository;

	@Autowired

	private NotifOutboxRepository outboxRepository;

	@Autowired

	private ObjectMapper objectMapper;

	@Autowired

	private WorkflowEngineTriggerResolver triggerResolver;

	@Autowired

	private WorkflowNotificationContextBuilder contextBuilder;

	@Autowired

	private JdbcTemplate jdbcTemplate;

	@Autowired

	private UserRepository userRepository;



	/**

	 * Invoked after a domain action succeeds (e.g. user or customer created, or HTTP endpoint completed).

	 * Failures are logged and do not roll back the business transaction.

	 */

	@Transactional(propagation = Propagation.REQUIRES_NEW)

	public void onActionCompleted(String actionCode, Long businessId, Map<String, Object> context) {

		if (actionCode == null || actionCode.isBlank()) {

			return;

		}

		String canonical = triggerResolver.normalizeTriggerCode(actionCode);

		Map<String, Object> ctx = contextBuilder.enrich(context, businessId);



		Optional<WorkflowDefinition> defOpt = definitionRepository.findBestPublished(canonical, businessId);

		if (!defOpt.isPresent() && !canonical.equalsIgnoreCase(actionCode.trim())) {

			defOpt = definitionRepository.findBestPublished(actionCode.trim().toUpperCase(Locale.ROOT), businessId);

		}

		if (!defOpt.isPresent()) {

			log.warn("[WF_ENGINE] no published pipeline for action={} (from trigger={}) businessId={} — "
					+ "check UM_WORKFLOW_DEFINITION.ACTION_CODE matches EP:{{endpointId}} for ENGINE_ACTION_CODE",
					canonical, actionCode, businessId);

			return;

		}



		WorkflowDefinition def = defOpt.get();

		List<WorkflowStep> steps = stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.getId(), 1);

		log.info("[WF_ENGINE] running pipeline defId={} action={} steps={} businessId={}",

				def.getId(), def.getActionCode(), steps.size(), businessId);

		runNotificationSteps(def, steps, businessId, ctx, TIMING_ON_ACTION_SUCCESS);

	}



	/**

	 * After final approval on a gateway-backed pipeline (from {@code UM_WORKFLOW_CONFIG.built_in_key}).

	 */

	public void onApprovalCompleted(Long definitionId, Long businessId, Map<String, Object> context) {

		if (definitionId == null) {

			return;

		}

		Optional<WorkflowDefinition> defOpt = definitionRepository.findById(definitionId);

		if (!defOpt.isPresent() || !"PUBLISHED".equalsIgnoreCase(defOpt.get().getStatus())) {

			return;

		}

		WorkflowDefinition def = defOpt.get();

		List<WorkflowStep> steps = stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.getId(), 1);

		Map<String, Object> ctx = contextBuilder.enrich(context, businessId);

		runNotificationSteps(def, steps, businessId, ctx, TIMING_AFTER_APPROVAL);

	}



	private void runNotificationSteps(WorkflowDefinition def, List<WorkflowStep> steps, Long businessId,

			Map<String, Object> ctx, String requiredTiming) {

		String triggerCode = def.getActionCode();

		for (WorkflowStep step : steps) {

			if (STEP_NOTIFICATION.equalsIgnoreCase(step.getStepType())) {

				enqueueNotificationStep(triggerCode, businessId, ctx, step, requiredTiming);

			} else if (STEP_APPROVAL.equalsIgnoreCase(step.getStepType())) {

				log.debug("[WF_ENGINE] APPROVAL step skipped at orchestrator (handled by gateway) defId={} order={}",

						def.getId(), step.getStepOrder());

			}

		}

	}



	private void enqueueNotificationStep(String actionCode, Long businessId, Map<String, Object> ctx,

			WorkflowStep step, String requiredTiming) {

		try {

			JsonNode cfg = objectMapper.readTree(step.getConfigJson());

			String timing = textOr(cfg, "timing", TIMING_ON_ACTION_SUCCESS);

			if (!requiredTiming.equalsIgnoreCase(timing)) {

				log.debug("[WF_ENGINE] skip notification step timing={} required={} action={}", timing,

						requiredTiming, actionCode);

				return;

			}



			String templateKey = textOr(cfg, "templateKey", null);

			if (templateKey == null || templateKey.isBlank()) {

				log.warn("[WF_ENGINE] notification step missing templateKey action={}", actionCode);

				return;

			}



			List<String> channels = resolveChannels(cfg);

			List<Recipient> recipients = resolveRecipients(cfg.path("audience"), ctx, businessId);

			if (recipients.isEmpty()) {

				log.warn("[WF_ENGINE] no recipient email for notification action={} template={} audience={}",

						actionCode, templateKey, textOr(cfg.path("audience"), "mode", "CONTEXT"));

				return;

			}



			for (Recipient recipient : recipients) {

				if (isBlank(recipient.email)) {

					continue;

				}

				String contextJson = objectMapper.writeValueAsString(buildTemplateContext(ctx, recipient));

				for (String channel : channels) {

					String idem = actionCode + ":" + requiredTiming + ":" + LocalDate.now() + ":" + step.getId() + ":"
							+ channel + ":" + recipient.type + ":"
							+ (recipient.id != null ? recipient.id : recipient.email);

					if (outboxRepository.findByIdempotencyKey(idem).isPresent()) {

						continue;

					}

					NotifOutbox row = new NotifOutbox();

					row.setActionCode(actionCode);

					row.setBusinessId(businessId);

					row.setChannel(channel);

					row.setTemplateKey(templateKey);

					row.setRecipientType(recipient.type);

					row.setRecipientId(recipient.id);

					row.setRecipientUsername(recipient.username);

					row.setRecipientEmail(recipient.email);

					row.setContextJson(contextJson);

					row.setStatus(STATUS_PENDING);

					row.setIdempotencyKey(idem);

					row.setAttempts(0);

					row.setCreatedAt(LocalDateTime.now());

					outboxRepository.save(row);

					log.info("[WF_ENGINE] outbox queued action={} channel={} template={} to={}", actionCode, channel,

							templateKey, recipient.email);

				}

			}

		} catch (Exception ex) {

			log.error("[WF_ENGINE] failed to enqueue notification action={}: {}", actionCode, ex.toString(), ex);

		}

	}



	private List<String> resolveChannels(JsonNode cfg) {

		List<String> channels = new ArrayList<>();

		JsonNode arr = cfg.path("channels");

		if (arr.isArray()) {

			for (JsonNode n : arr) {

				String ch = n.asText("").trim().toUpperCase(Locale.ROOT);

				if (ch.isEmpty()) {

					continue;

				}

				if (CHANNEL_BOTH.equals(ch)) {

					channels.add(CHANNEL_EMAIL);

					channels.add(CHANNEL_INBOX);

				} else {

					channels.add(ch);

				}

			}

		}

		if (channels.isEmpty()) {

			channels.add(CHANNEL_EMAIL);

		}

		return channels;

	}



	private List<Recipient> resolveRecipients(JsonNode audience, Map<String, Object> ctx, Long businessId) {

		String mode = textOr(audience, "mode", "CONTEXT").trim().toUpperCase(Locale.ROOT);

		if ("ROLES".equals(mode) || "ROLE".equals(mode)) {

			return recipientsForRoles(audience.path("roleNames"), businessId);

		}

		String contextRecipient = textOr(audience, "contextRecipient", "createdUser");

		if ("CONTEXT".equals(mode)) {

			if ("createdCustomer".equalsIgnoreCase(contextRecipient)) {

				Recipient one = recipientFromCustomer(ctx);

				return one == null ? Collections.emptyList() : Collections.singletonList(one);

			}

			Recipient one = recipientFromUser(ctx);

			return one == null ? Collections.emptyList() : Collections.singletonList(one);

		}

		Recipient one = recipientFromUser(ctx);

		return one == null ? Collections.emptyList() : Collections.singletonList(one);

	}



	private List<Recipient> recipientsForRoles(JsonNode roleNamesNode, Long businessId) {

		List<String> roleNames = new ArrayList<>();

		if (roleNamesNode.isArray()) {

			for (JsonNode n : roleNamesNode) {

				if (n.isTextual() && !n.asText("").trim().isEmpty()) {

					roleNames.add(n.asText().trim());

				}

			}

		}

		if (roleNames.isEmpty()) {

			return Collections.emptyList();

		}

		String in = String.join(",", Collections.nCopies(roleNames.size(), "?"));

		List<Object> args = new ArrayList<>(roleNames);

		args.add(businessId);

		args.add(businessId);

		String sql = "SELECT DISTINCT u.ID, u.USERNAME, u.EMAIL, u.FIRST_NAME, u.LAST_NAME "

				+ "FROM UM.UM_USER u "

				+ "JOIN UM.UM_USER_ROLE ur ON ur.USER_ID = u.ID "

				+ "JOIN UM.UM_ROLE r ON r.ID = ur.ROLE_ID "

				+ "WHERE r.NAME IN (" + in + ") "

				+ "AND (u.BUSINESS_ID IS NULL OR u.BUSINESS_ID = ? OR ? IS NULL) "

				+ "AND u.EMAIL IS NOT NULL";

		Set<String> seenEmails = new LinkedHashSet<>();

		List<Recipient> out = new ArrayList<>();

		jdbcTemplate.query(sql, rs -> {

			String email = rs.getString("EMAIL");

			if (email == null || email.trim().isEmpty()) {

				return;

			}

			String normalized = email.trim().toLowerCase(Locale.ROOT);

			if (!seenEmails.add(normalized)) {

				return;

			}

			Recipient r = new Recipient();

			r.type = "USER";

			r.id = rs.getLong("ID");

			r.username = rs.getString("USERNAME");

			r.email = email.trim();

			r.firstName = rs.getString("FIRST_NAME");

			r.lastName = rs.getString("LAST_NAME");

			out.add(r);

		}, args.toArray());

		return out;

	}



	private Recipient recipientFromUser(Map<String, Object> ctx) {

		Long userId = longVal(ctx.get("userId"));

		String email = str(ctx.get("email"));

		String username = str(ctx.get("username"));

		if (userId == null && (email == null || email.isBlank()) && (username == null || username.isBlank())) {

			return null;

		}

		Recipient r = new Recipient();

		r.type = "USER";

		r.id = userId;

		r.username = username;

		r.email = email;

		r.firstName = str(ctx.get("firstName"));

		r.lastName = str(ctx.get("lastName"));

		if ((r.email == null || r.email.isBlank()) && userId != null) {

			userRepository.findById(userId).ifPresent(u -> hydrateRecipientFromUser(r, u));

		}

		if ((r.email == null || r.email.isBlank()) && username != null && !username.isBlank()) {

			userRepository.findFirstByUsernameOrderByIdAsc(username.trim())

					.ifPresent(u -> hydrateRecipientFromUser(r, u));

		}

		if (r.email == null || r.email.isBlank()) {

			return null;

		}

		return r;

	}



	private static void hydrateRecipientFromUser(Recipient r, User u) {

		if (r.id == null) {

			r.id = u.getId();

		}

		if (r.username == null || r.username.isBlank()) {

			r.username = u.getUsername();

		}

		if (r.email == null || r.email.isBlank()) {

			r.email = u.getEmail();

		}

		if (r.firstName == null || r.firstName.isBlank()) {

			r.firstName = u.getFirstName();

		}

		if (r.lastName == null || r.lastName.isBlank()) {

			r.lastName = u.getLastName();

		}

	}



	private Recipient recipientFromCustomer(Map<String, Object> ctx) {

		Long customerId = longVal(ctx.get("customerId"));

		String email = str(ctx.get("email"));

		if (customerId == null && (email == null || email.isBlank())) {

			return null;

		}

		Recipient r = new Recipient();

		r.type = "CUSTOMER";

		r.id = customerId;

		r.email = email;

		r.firstName = str(ctx.get("firstName"));

		r.lastName = str(ctx.get("lastName"));

		r.displayName = str(ctx.get("name"));

		if ((r.email == null || r.email.isBlank()) && customerId != null) {

			try {

				jdbcTemplate.query(

						"SELECT EMAIL, FIRST_NAME, LAST_NAME, FULL_NAME FROM UM.KYC_CUSTOMER WHERE ID = ?",

						rs -> {

							if (rs.next()) {

								if (r.email == null || r.email.isBlank()) {

									r.email = rs.getString("EMAIL");

								}

								if (r.firstName == null || r.firstName.isBlank()) {

									r.firstName = rs.getString("FIRST_NAME");

								}

								if (r.lastName == null || r.lastName.isBlank()) {

									r.lastName = rs.getString("LAST_NAME");

								}

								if (r.displayName == null || r.displayName.isBlank()) {

									r.displayName = rs.getString("FULL_NAME");

								}

							}

						},

						customerId);

			} catch (Exception ex) {

				log.debug("[WF_ENGINE] could not load customer {} for notification: {}", customerId, ex.toString());

			}

		}

		if (r.displayName == null) {

			r.displayName = joinName(r.firstName, r.lastName);

		}

		if (r.email == null || r.email.isBlank()) {

			return null;

		}

		return r;

	}



	private Map<String, String> buildTemplateContext(Map<String, Object> ctx, Recipient r) {

		Map<String, String> vars = new HashMap<>();

		vars.put("firstName", nullToEmpty(r.firstName != null ? r.firstName : str(ctx.get("firstName"))));

		vars.put("lastName", nullToEmpty(r.lastName != null ? r.lastName : str(ctx.get("lastName"))));

		vars.put("username", nullToEmpty(r.username != null ? r.username : str(ctx.get("username"))));

		vars.put("email", nullToEmpty(r.email));

		vars.put("name", nullToEmpty(r.displayName != null ? r.displayName : str(ctx.get("name"))));

		vars.put("date", nullToEmpty(str(ctx.get("date"))));
		if (vars.get("date").isEmpty()) {
			vars.put("date", LocalDate.now().toString());
		}
		vars.put("promoCode", nullToEmpty(str(ctx.get("promoCode"))));

		return vars;

	}



	private static String textOr(JsonNode node, String field, String defaultVal) {

		if (node == null || node.isMissingNode()) {

			return defaultVal;

		}

		JsonNode v = node.get(field);

		if (v == null || v.isNull()) {

			return defaultVal;

		}

		String s = v.asText();

		return s != null && !s.isBlank() ? s : defaultVal;

	}



	private static String str(Object o) {

		return o != null ? String.valueOf(o) : null;

	}



	private static Long longVal(Object o) {

		if (o == null) {

			return null;

		}

		if (o instanceof Number) {

			return ((Number) o).longValue();

		}

		try {

			return Long.parseLong(o.toString());

		} catch (NumberFormatException e) {

			return null;

		}

	}



	private static String nullToEmpty(String s) {

		return s != null ? s : "";

	}



	private static String joinName(String first, String last) {

		String f = first != null ? first.trim() : "";

		String l = last != null ? last.trim() : "";

		return (f + " " + l).trim();

	}



	private static boolean isBlank(String s) {

		return s == null || s.isBlank();

	}



	private static final class Recipient {

		String type;

		Long id;

		String username;

		String email;

		String firstName;

		String lastName;

		String displayName;

	}

}


