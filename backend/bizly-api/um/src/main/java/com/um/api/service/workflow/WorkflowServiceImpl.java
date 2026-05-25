package com.um.api.service.workflow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.api.dto.menu.MenuPermissionRouteRow;
import com.um.api.dto.menu.PermissionMetadataResponse;
import com.um.api.dto.workflow.ApproveWorkflowRequest;
import com.um.api.dto.workflow.BusinessRegistrationWorkflowRequest;
import com.um.api.dto.workflow.CreateWorkflowConfigRequest;
import com.um.api.dto.workflow.DeleteWorkflowConfigRequest;
import com.um.api.dto.workflow.UpdateWorkflowConfigRequest;
import com.um.api.dto.workflow.WorkflowCatalogScreenDto;
import com.um.api.dto.workflow.WorkflowConfigRowDto;
import com.um.api.dto.workflow.WorkflowInstanceRowDto;
import com.um.api.dto.workflow.WorkflowQueueQueryRequest;
import com.um.api.model.user.User;
import com.um.api.model.workflow.WorkflowApiEndpoint;
import com.um.api.model.workflow.WorkflowConfig;
import com.um.api.model.workflow.WorkflowInstance;
import com.um.api.repository.user.UserRepository;
import com.um.api.repository.workflow.WorkflowApiEndpointRepository;
import com.um.api.repository.workflow.WorkflowConfigRepository;
import com.um.api.repository.workflow.WorkflowInstanceRepository;
import com.um.api.service.menu.MenuPermissionMetadataService;
import com.um.api.service.security.MenuPermissionService;
import com.um.common.ApiMessages;
import com.um.exception.ServiceException;
import com.um.api.service.workflow.engine.WorkflowEngineActionCodes;
import com.um.api.service.workflow.engine.WorkflowEngineApprovalRuntimeService;
import com.um.api.service.workflow.engine.WorkflowEngineOrchestratorService;
import com.um.api.service.workflow.engine.WorkflowEngineRegistrationService;
import com.um.api.service.workflow.engine.WorkflowNotificationContextBuilder;
import com.um.security.BusinessContextHolder;

@Service
public class WorkflowServiceImpl implements IWorkflowService {

	public static final String BUILTIN_BUSINESS_REGISTRATION = "BUSINESS_REGISTRATION";

	@Autowired
	private WorkflowConfigRepository configRepository;
	@Autowired
	private WorkflowInstanceRepository instanceRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MenuPermissionMetadataService menuPermissionMetadataService;
	@Autowired
	private MenuPermissionService menuPermissionService;
	@Autowired
	private ObjectMapper objectMapper;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private WorkflowHttpReplayExecutor workflowHttpReplayExecutor;
	@Autowired
	private WorkflowEngineOrchestratorService workflowEngineOrchestrator;
	@Autowired
	private WorkflowEngineApprovalRuntimeService approvalRuntime;
	@Autowired
	private WorkflowEngineRegistrationService registrationService;
	@Autowired
	private WorkflowApiEndpointRepository workflowApiEndpointRepository;
	@Autowired
	private WorkflowEnforcementHelper workflowEnforcement;
	@Autowired
	private WorkflowNotificationContextBuilder notificationContextBuilder;

	@Override
	@Transactional
	public Long createBusinessRegistrationIfNeeded(String username, BusinessRegistrationWorkflowRequest req) {
		User user = userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		if (user.getBusinessId() == null || !user.getBusinessId().equals(req.getBusinessId())) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		Long ctxBid = BusinessContextHolder.currentBusinessId();
		if (!BusinessContextHolder.canBypassTenant() && (ctxBid == null || !ctxBid.equals(req.getBusinessId()))) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}

		Optional<WorkflowConfig> cfgOpt = registrationService.resolveActiveRegistrationConfig();
		if (cfgOpt.isEmpty()) {
			return null;
		}
		WorkflowConfig cfg = cfgOpt.get();

		if (instanceRepository.existsByBusinessIdAndWorkflowConfigIdAndStatusIgnoreCase(req.getBusinessId(),
				cfg.getId(), "PENDING")) {
			return instanceRepository
					.findByBusinessIdAndStatusOrderByCreatedAtDesc(req.getBusinessId(), "PENDING")
					.stream()
					.filter(i -> cfg.getId().equals(i.getWorkflowConfigId()))
					.findFirst()
					.map(WorkflowInstance::getId)
					.orElse(null);
		}

		WorkflowInstance inst = new WorkflowInstance();
		inst.setWorkflowConfigId(cfg.getId());
		inst.setBusinessId(req.getBusinessId());
		inst.setTriggeredByUserId(user.getId());
		inst.setTriggeredByUsername(username);
		inst.setCurrentLevel(1);
		inst.setStatus("PENDING");
		inst.setScreenName(cfg.getScreenName());
		inst.setActionName(cfg.getActionName());
		inst.setCreatedAt(LocalDateTime.now());
		inst.setUpdatedAt(LocalDateTime.now());
		try {
			Map<String, Object> payload = new HashMap<>();
			payload.put("businessName", req.getBusinessName());
			payload.put("businessId", req.getBusinessId());
			inst.setPayloadJson(objectMapper.writeValueAsString(payload));
		} catch (Exception e) {
			throw new ServiceException("Could not serialise workflow payload", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return instanceRepository.save(inst).getId();
	}

	@Override
	public List<WorkflowInstanceRowDto> queue(WorkflowQueueQueryRequest query) {
		if (query == null) {
			query = new WorkflowQueueQueryRequest();
		}
		Long bid = BusinessContextHolder.currentBusinessId();
		boolean portalAdmin = BusinessContextHolder.isPortalAdminRoleLevel();

		if (bid == null && !portalAdmin) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}

		List<WorkflowInstance> rows;
		String st = query.getStatus();
		if (bid == null) {
			List<Long> builtInCfgIds = configRepository.findAll().stream()
					.filter(c -> c.getBuiltInKey() != null && !c.getBuiltInKey().isBlank())
					.map(WorkflowConfig::getId)
					.collect(Collectors.toList());
			if (builtInCfgIds.isEmpty()) {
				rows = List.of();
			} else if (st != null && !st.isBlank()) {
				rows = instanceRepository.findByWorkflowConfigIdInAndStatusOrderByCreatedAtDesc(builtInCfgIds,
						st.trim().toUpperCase());
			} else {
				rows = instanceRepository.findByWorkflowConfigIdInOrderByCreatedAtDesc(builtInCfgIds);
			}
		} else {
			if (st != null && !st.isBlank()) {
				rows = instanceRepository.findByBusinessIdAndStatusOrderByCreatedAtDesc(bid, st.trim().toUpperCase());
			} else {
				rows = instanceRepository.findByBusinessIdOrderByCreatedAtDesc(bid);
			}
		}

		rows = applyQueueFilters(rows, query);

		String username = currentUsername();
		List<String> roleNames = currentRoleNames();
		Map<Long, WorkflowConfig> cfgCache = new HashMap<>();
		for (WorkflowInstance inst : rows) {
			cfgCache.computeIfAbsent(inst.getWorkflowConfigId(), id -> configRepository.findById(id).orElse(null));
		}

		return rows.stream()
				.filter(inst -> mayViewQueueRow(inst, cfgCache.get(inst.getWorkflowConfigId()), roleNames, username))
				.map(inst -> mapRow(inst, cfgCache.get(inst.getWorkflowConfigId()), roleNames, username))
				.collect(Collectors.toList());
	}

	private List<WorkflowInstance> applyQueueFilters(List<WorkflowInstance> rows, WorkflowQueueQueryRequest q) {
		if (rows.isEmpty()) {
			return rows;
		}
		LocalDate from = parseDateOrNull(q.getDateFrom());
		LocalDate to = parseDateOrNull(q.getDateTo());
		String screenExact = trimExact(q.getScreenRoute());
		String actionExact = trimExact(q.getActionName());
		String screenQ = trimLower(q.getScreenNameContains());
		String actionQ = trimLower(q.getActionNameContains());
		String makerQ = trimLower(q.getMakerUsernameContains());
		return rows.stream().filter(inst -> {
			if (screenExact != null) {
				String s = inst.getScreenName() == null ? "" : inst.getScreenName();
				if (!s.equalsIgnoreCase(screenExact)) {
					return false;
				}
			}
			if (actionExact != null) {
				String s = inst.getActionName() == null ? "" : inst.getActionName();
				if (!s.equalsIgnoreCase(actionExact)) {
					return false;
				}
			}
			if (screenQ != null) {
				String s = inst.getScreenName() == null ? "" : inst.getScreenName().toLowerCase();
				if (!s.contains(screenQ)) {
					return false;
				}
			}
			if (actionQ != null) {
				String s = inst.getActionName() == null ? "" : inst.getActionName().toLowerCase();
				if (!s.contains(actionQ)) {
					return false;
				}
			}
			if (makerQ != null) {
				String s = inst.getTriggeredByUsername() == null ? "" : inst.getTriggeredByUsername().toLowerCase();
				if (!s.contains(makerQ)) {
					return false;
				}
			}
			if (from != null && inst.getCreatedAt() != null && inst.getCreatedAt().toLocalDate().isBefore(from)) {
				return false;
			}
			if (to != null && inst.getCreatedAt() != null && inst.getCreatedAt().toLocalDate().isAfter(to)) {
				return false;
			}
			return true;
		}).collect(Collectors.toList());
	}

	private static LocalDate parseDateOrNull(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(raw.trim());
		} catch (Exception e) {
			return null;
		}
	}

	private static String trimLower(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return s.trim().toLowerCase();
	}

	private static String trimExact(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return s.trim();
	}

	private boolean isCurrentUserMakerParticipant(WorkflowInstance inst, WorkflowConfig cfg) {
		String username = currentUsername();
		if (username == null || inst == null || cfg == null) {
			return false;
		}
		boolean self = inst.getTriggeredByUsername() != null
				&& username.equalsIgnoreCase(inst.getTriggeredByUsername().trim());
		return self || isMakerSide(cfg, currentRoleNames(), username, inst.getBusinessId());
	}

	private boolean mayViewQueueRow(WorkflowInstance inst, WorkflowConfig cfg, List<String> roleNamesUpper,
			String username) {
		if (inst == null || cfg == null || username == null || username.isBlank()) {
			return false;
		}
		boolean self = inst.getTriggeredByUsername() != null
				&& username.equalsIgnoreCase(inst.getTriggeredByUsername().trim());
		Long bizId = inst.getBusinessId();
		boolean makerSide = self || isMakerSide(cfg, roleNamesUpper, username, bizId);
		boolean pending = "PENDING".equalsIgnoreCase(inst.getStatus());
		boolean checkerAtLevel = isCheckerForTier(cfg, roleNamesUpper, username, inst.getCurrentLevel(), bizId);
		boolean checkerSide = pending && checkerAtLevel;
		return makerSide || checkerSide;
	}

	private boolean isMakerSide(WorkflowConfig cfg, List<String> roleNamesUpper, String username, Long businessId) {
		return workflowEnforcement.isMakerSide(cfg, roleNamesUpper, username, businessId);
	}

	/**
	 * For gateway-deferred instances, {@code payload_json} stores path/method metadata; the UI queue should show
	 * screen/action plus the submitted JSON body when available.
	 */
	private String buildQueueViewerPayload(WorkflowInstance i) {
		String rawBody = i.getCapturedBody();
		if (rawBody != null && !rawBody.isBlank()) {
			try {
				JsonNode body = objectMapper.readTree(rawBody.trim());
				ObjectNode root = objectMapper.createObjectNode();
				if (i.getScreenName() != null && !i.getScreenName().isBlank()) {
					root.put("screen", i.getScreenName());
				}
				if (i.getActionName() != null && !i.getActionName().isBlank()) {
					root.put("action", i.getActionName());
				}
				root.set("submittedData", body);
				return objectMapper.writeValueAsString(root);
			} catch (Exception e) {
				return rawBody.trim();
			}
		}
		return i.getPayloadJson();
	}

	private WorkflowInstanceRowDto mapRow(WorkflowInstance i, WorkflowConfig cfg, List<String> roleNamesUpper,
			String username) {
		WorkflowInstanceRowDto d = new WorkflowInstanceRowDto();
		d.setId(i.getId());
		d.setScreenName(i.getScreenName());
		d.setActionName(i.getActionName());
		d.setTriggeredByUsername(i.getTriggeredByUsername());
		d.setCurrentLevel(i.getCurrentLevel());
		d.setStatus(i.getStatus());
		d.setCreatedAt(i.getCreatedAt());
		d.setBusinessId(i.getBusinessId());
		d.setPayloadJson(buildQueueViewerPayload(i));
		int levels = cfg != null && cfg.getLevelCount() != null ? cfg.getLevelCount() : 1;
		d.setLevelRequired(levels);
		d.setLevelCompleted(i.getCurrentLevel());
		d.setCheckerSummary(summarizeChecker(cfg));
		d.setCheckerComment(i.getCheckerComment());
		boolean pending = "PENDING".equalsIgnoreCase(i.getStatus());
		boolean self = username != null && i.getTriggeredByUsername() != null
				&& username.equalsIgnoreCase(i.getTriggeredByUsername());
		boolean makerParticipant = self || isMakerSide(cfg, roleNamesUpper, username, i.getBusinessId());
		d.setMakerParticipant(makerParticipant);
		boolean checker = isCheckerForTier(cfg, roleNamesUpper, username, i.getCurrentLevel(), i.getBusinessId());
		d.setCanApprove(pending && checker && !makerParticipant);
		return d;
	}

	private String summarizeChecker(WorkflowConfig cfg) {
		if (cfg == null) {
			return "";
		}
		List<List<String>> roleTiers = parseTierMatrix(cfg.getCheckerRolesJson());
		List<List<String>> userTiers = parseTierMatrix(cfg.getCheckerUsersJson());
		int n = Math.max(Math.max(1, roleTiers.size()), userTiers.size());
		StringBuilder sb = new StringBuilder();
		for (int t = 0; t < n; t++) {
			List<String> r = t < roleTiers.size() ? roleTiers.get(t) : List.of();
			List<String> u = t < userTiers.size() ? userTiers.get(t) : List.of();
			if (r.isEmpty() && u.isEmpty()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(" | ");
			}
			sb.append("L").append(t + 1).append(": ");
			if (!u.isEmpty()) {
				sb.append("users ").append(String.join(", ", u));
			} else if (!r.isEmpty()) {
				sb.append("roles ").append(String.join(", ", r));
			}
		}
		return sb.toString();
	}

	@Override
	@Transactional
	public void approve(ApproveWorkflowRequest req) {
		WorkflowInstance inst = instanceRepository.findById(req.getInstanceId())
				.orElseThrow(() -> new ServiceException("Workflow instance not found", HttpStatus.NOT_FOUND));
		assertBusinessScopeForAction(inst);
		if (!"PENDING".equalsIgnoreCase(inst.getStatus())) {
			throw new ServiceException("Instance is not pending", HttpStatus.CONFLICT);
		}
		WorkflowConfig cfg = configRepository.findById(inst.getWorkflowConfigId())
				.orElseThrow(() -> new ServiceException("Workflow configuration missing", HttpStatus.NOT_FOUND));
		if (isCurrentUserMakerParticipant(inst, cfg)) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		if (!isCheckerForTier(cfg, currentRoleNames(), currentUsername(), inst.getCurrentLevel(), inst.getBusinessId())) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		int levels = cfg.getLevelCount() == null ? 1 : cfg.getLevelCount();
		if (inst.getCurrentLevel() < levels) {
			inst.setCurrentLevel(inst.getCurrentLevel() + 1);
			inst.setUpdatedAt(LocalDateTime.now());
			instanceRepository.save(inst);
			return;
		}
		inst.setStatus("APPROVED");
		String note = req.getComment() == null ? null : req.getComment().trim();
		if (note != null && !note.isBlank()) {
			inst.setCheckerComment(note);
		}
		inst.setUpdatedAt(LocalDateTime.now());
		instanceRepository.save(inst);
		syncBuiltinBusinessRegistration(inst, cfg, true);
		Long bizId = inst.getBusinessId();
		Map<String, Object> notifCtx = notificationContextBuilder.enrich(approvalNotificationContext(inst), bizId);
		approvalRuntime.parseDefinitionIdFromBuiltInKey(cfg.getBuiltInKey())
				.ifPresent(defId -> workflowEngineOrchestrator.onApprovalCompleted(defId, bizId, notifCtx));
		workflowHttpReplayExecutor.replayIfCaptured(inst);
		if (inst.getEndpointId() != null) {
			workflowEngineOrchestrator.onActionCompleted(
					WorkflowEngineActionCodes.forEndpoint(inst.getEndpointId()), bizId, notifCtx);
		}
	}

	private Map<String, Object> approvalNotificationContext(WorkflowInstance inst) {
		Map<String, Object> ctx = new HashMap<>();
		ctx.put("businessId", inst.getBusinessId());
		ctx.put("workflowInstanceId", inst.getId());
		if (inst.getTriggeredByUserId() != null) {
			ctx.put("userId", inst.getTriggeredByUserId());
		}
		if (inst.getTriggeredByUsername() != null) {
			ctx.put("username", inst.getTriggeredByUsername());
		}
		userRepository.findFirstByUsernameOrderByIdAsc(
				inst.getTriggeredByUsername() == null ? "" : inst.getTriggeredByUsername()).ifPresent(u -> {
			ctx.put("email", u.getEmail());
			ctx.put("firstName", u.getFirstName());
			ctx.put("lastName", u.getLastName());
		});
		return ctx;
	}

	@Override
	@Transactional
	public void reject(ApproveWorkflowRequest req) {
		WorkflowInstance inst = instanceRepository.findById(req.getInstanceId())
				.orElseThrow(() -> new ServiceException("Workflow instance not found", HttpStatus.NOT_FOUND));
		assertBusinessScopeForAction(inst);
		if (!"PENDING".equalsIgnoreCase(inst.getStatus())) {
			throw new ServiceException("Instance is not pending", HttpStatus.CONFLICT);
		}
		WorkflowConfig cfg = configRepository.findById(inst.getWorkflowConfigId())
				.orElseThrow(() -> new ServiceException("Workflow configuration missing", HttpStatus.NOT_FOUND));
		if (isCurrentUserMakerParticipant(inst, cfg)) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		if (!isCheckerForTier(cfg, currentRoleNames(), currentUsername(), inst.getCurrentLevel(), inst.getBusinessId())) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		inst.setStatus("REJECTED");
		String note = req.getComment() == null ? null : req.getComment().trim();
		inst.setCheckerComment(note == null || note.isBlank() ? null : note);
		inst.setUpdatedAt(LocalDateTime.now());
		instanceRepository.save(inst);
		syncBuiltinBusinessRegistration(inst, cfg, false);
	}

	private void syncBuiltinBusinessRegistration(WorkflowInstance inst, WorkflowConfig cfg, boolean approved) {
		if (!registrationService.isRegistrationConfig(cfg)) {
			return;
		}
		Long businessId = inst.getBusinessId();
		if (businessId == null) {
			return;
		}
		String status = approved ? "ACTIVE" : "REJECTED";
		int updated = entityManager.createNativeQuery(
				"UPDATE UM.UM_BUSINESS SET STATUS = :st, UPDATED_AT = SYSTIMESTAMP WHERE ID = :id")
				.setParameter("st", status)
				.setParameter("id", businessId)
				.executeUpdate();
		if (updated == 0) {
			throw new ServiceException("Business row not found for workflow completion", HttpStatus.CONFLICT);
		}
		if (approved && inst.getTriggeredByUserId() != null) {
			userRepository.findById(inst.getTriggeredByUserId()).ifPresent(u -> {
				/* After governance approval, send the user through the normal welcome wizard once. */
				u.setFirstLogin(1);
				u.setWelcomeCompletedAt(null);
				userRepository.save(u);
			});
		}
	}

	@Override
	public List<WorkflowConfigRowDto> listWorkflowConfigs(String username) {
		userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long ctx = BusinessContextHolder.currentBusinessId();
		boolean portalAdmin = BusinessContextHolder.isPortalAdminRoleLevel();
		List<WorkflowConfig> all = configRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
		return all.stream().filter(c -> {
			if (portalAdmin && ctx == null) {
				return true;
			}
			if (portalAdmin) {
				Long cb = c.getBusinessId();
				return cb == null || (ctx != null && cb.equals(ctx));
			}
			Long cb = c.getBusinessId();
			return cb == null || (ctx != null && cb.equals(ctx));
		}).map(this::toConfigRow).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void updateWorkflowConfig(String username, UpdateWorkflowConfigRequest req) {
		userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		WorkflowConfig cfg = configRepository.findById(req.getId())
				.orElseThrow(() -> new ServiceException("Workflow configuration not found", HttpStatus.NOT_FOUND));
		assertConfigVisibility(cfg);
		assertDeletableByTenant(cfg);
		if (cfg.getBuiltInKey() != null && !cfg.getBuiltInKey().isBlank()) {
			throw new ServiceException("System workflow definitions cannot be modified.", HttpStatus.BAD_REQUEST);
		}
		if (req.getHasWorkflow() != null) {
			cfg.setHasWorkflow(req.getHasWorkflow());
		}
		if (req.getMakerRolesJson() != null) {
			validateFlexibleJsonArray(req.getMakerRolesJson(), "makerRolesJson");
			cfg.setMakerRolesJson(req.getMakerRolesJson().isBlank() ? null : req.getMakerRolesJson().trim());
		}
		if (req.getCheckerRolesJson() != null) {
			validateFlexibleJsonArray(req.getCheckerRolesJson(), "checkerRolesJson");
			cfg.setCheckerRolesJson(req.getCheckerRolesJson().isBlank() ? null : req.getCheckerRolesJson().trim());
		}
		if (req.getMakerUsersJson() != null) {
			validateFlexibleJsonArray(req.getMakerUsersJson(), "makerUsersJson");
			cfg.setMakerUsersJson(req.getMakerUsersJson().isBlank() ? null : req.getMakerUsersJson().trim());
		}
		if (req.getCheckerUsersJson() != null) {
			validateFlexibleJsonArray(req.getCheckerUsersJson(), "checkerUsersJson");
			cfg.setCheckerUsersJson(req.getCheckerUsersJson().isBlank() ? null : req.getCheckerUsersJson().trim());
		}
		applyDerivedLevelCount(cfg);
		if (cfg.isHasWorkflow()) {
			validateCheckerPresent(cfg);
		}
		configRepository.save(cfg);
	}

	@Override
	public List<WorkflowCatalogScreenDto> catalogScreens(String username) {
		userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		PermissionMetadataResponse meta = permissionMetadataForPrincipal();
		Map<Long, List<String>> byMenu = meta.getActionsByMenuId() == null ? Map.of() : meta.getActionsByMenuId();
		List<WorkflowCatalogScreenDto> out = new ArrayList<>();
		for (MenuPermissionRouteRow row : meta.getMenus() == null ? List.<MenuPermissionRouteRow>of() : meta.getMenus()) {
			if (row.getRoute() == null || row.getRoute().isBlank()) {
				continue;
			}
			WorkflowCatalogScreenDto d = new WorkflowCatalogScreenDto();
			d.setRoute(row.getRoute().trim());
			d.setMenuLabel(row.getMenuPath() != null && !row.getMenuPath().isBlank() ? row.getMenuPath() : row.getRoute());
			Long mid = row.getMenuId();
			List<String> actions = mid == null ? List.of() : byMenu.getOrDefault(mid, List.of());
			d.setActions(new ArrayList<>(actions));
			out.add(d);
		}
		out.sort((a, b) -> {
			String la = a.getMenuLabel() == null ? "" : a.getMenuLabel();
			String lb = b.getMenuLabel() == null ? "" : b.getMenuLabel();
			return la.compareToIgnoreCase(lb);
		});
		return out;
	}

	@Override
	public List<String> mutatingWorkflowActions(String username) {
		userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		PermissionMetadataResponse meta = permissionMetadataForPrincipal();
		List<String> raw = meta.getActions() == null ? List.of() : new ArrayList<>(meta.getActions());
		return new ArrayList<>(raw);
	}

	@Override
	@Transactional
	public void createWorkflowConfig(String username, CreateWorkflowConfigRequest req) {
		userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		boolean bypass = BusinessContextHolder.canBypassTenant();
		Long ctx = BusinessContextHolder.currentBusinessId();
		Long targetBusinessId = resolveTargetBusinessIdForWrite(req.getBusinessId(), bypass, ctx);

		String screen;
		String action;
		if (req.getEndpointId() != null) {
			WorkflowApiEndpoint ep = workflowApiEndpointRepository.findById(req.getEndpointId())
					.orElseThrow(() -> new ServiceException("Unknown workflow API endpoint id.", HttpStatus.BAD_REQUEST));
			if (!ep.isActive() || ep.getPathAntPattern() == null || ep.getPathAntPattern().isBlank()) {
				throw new ServiceException("Workflow API endpoint is inactive or has no path pattern.",
						HttpStatus.BAD_REQUEST);
			}
			String ac = ep.getActionCode() == null ? "" : ep.getActionCode().trim();
			String acu = ac.toUpperCase(Locale.ROOT);
			if (!"ADD".equals(acu) && !"EDIT".equals(acu) && !"DELETE".equals(acu)) {
				throw new ServiceException("Workflow API endpoint action must be ADD, EDIT, or DELETE.",
						HttpStatus.BAD_REQUEST);
			}
			screen = ep.getScreenRoute() == null ? "" : ep.getScreenRoute().trim();
			action = ac;
			if (screen.isEmpty()) {
				throw new ServiceException("Workflow API endpoint has no screen route.", HttpStatus.BAD_REQUEST);
			}
		} else {
			screen = req.getScreenName() == null ? "" : req.getScreenName().trim();
			action = req.getActionName() == null ? "" : req.getActionName().trim();
			if (screen.isEmpty() || action.isEmpty()) {
				throw new ServiceException("screenName and actionName are required", HttpStatus.BAD_REQUEST);
			}
			PermissionMetadataResponse permMeta = menuPermissionMetadataService.buildMetadata();
			assertWorkflowActionForScreen(screen, action, permMeta);
		}

		for (WorkflowConfig existing : configRepository.findByScreenNameIgnoreCaseAndActionNameIgnoreCase(screen,
				action)) {
			if (Objects.equals(existing.getBusinessId(), targetBusinessId)) {
				throw new ServiceException("A workflow already exists for this screen, action, and business scope.",
						HttpStatus.CONFLICT);
			}
		}

		WorkflowConfig cfg = new WorkflowConfig();
		cfg.setScreenName(screen);
		cfg.setActionName(action);
		cfg.setHasWorkflow(Boolean.TRUE.equals(req.getHasWorkflow()));
		if (req.getMakerRolesJson() != null) {
			validateFlexibleJsonArray(req.getMakerRolesJson(), "makerRolesJson");
			cfg.setMakerRolesJson(req.getMakerRolesJson().isBlank() ? null : req.getMakerRolesJson().trim());
		}
		if (req.getCheckerRolesJson() != null) {
			validateFlexibleJsonArray(req.getCheckerRolesJson(), "checkerRolesJson");
			cfg.setCheckerRolesJson(req.getCheckerRolesJson().isBlank() ? null : req.getCheckerRolesJson().trim());
		}
		if (req.getMakerUsersJson() != null) {
			validateFlexibleJsonArray(req.getMakerUsersJson(), "makerUsersJson");
			cfg.setMakerUsersJson(req.getMakerUsersJson().isBlank() ? null : req.getMakerUsersJson().trim());
		}
		if (req.getCheckerUsersJson() != null) {
			validateFlexibleJsonArray(req.getCheckerUsersJson(), "checkerUsersJson");
			cfg.setCheckerUsersJson(req.getCheckerUsersJson().isBlank() ? null : req.getCheckerUsersJson().trim());
		}
		cfg.setBuiltInKey(null);
		cfg.setBusinessId(targetBusinessId);
		cfg.setCreatedBy(username);
		cfg.setCreatedAt(LocalDateTime.now());
		applyDerivedLevelCount(cfg);
		if (cfg.isHasWorkflow()) {
			validateCheckerPresent(cfg);
		}
		configRepository.save(cfg);
	}

	@Override
	@Transactional
	public void deleteWorkflowConfig(String username, DeleteWorkflowConfigRequest req) {
		userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		WorkflowConfig cfg = configRepository.findById(req.getId())
				.orElseThrow(() -> new ServiceException("Workflow configuration not found", HttpStatus.NOT_FOUND));
		assertConfigVisibility(cfg);
		assertDeletableByTenant(cfg);
		if (cfg.getBuiltInKey() != null && !cfg.getBuiltInKey().isBlank()) {
			throw new ServiceException("System workflow definitions cannot be deleted.", HttpStatus.BAD_REQUEST);
		}
		instanceRepository.deleteByWorkflowConfigId(cfg.getId());
		configRepository.delete(cfg);
	}

	private Long resolveTargetBusinessIdForWrite(Long requestedBusinessId, boolean bypass, Long ctx) {
		if (bypass || BusinessContextHolder.isPortalAdminRoleLevel()) {
			if (requestedBusinessId != null) {
				return requestedBusinessId;
			}
			return ctx;
		}
		if (ctx == null) {
			throw new ServiceException("Select a business context before managing workflow configuration.",
					HttpStatus.BAD_REQUEST);
		}
		if (requestedBusinessId != null && !requestedBusinessId.equals(ctx)) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		return ctx;
	}

	private void assertDeletableByTenant(WorkflowConfig cfg) {
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			return;
		}
		if (cfg.getBusinessId() == null) {
			throw new ServiceException("Only a system administrator may change global workflow rows.",
					HttpStatus.FORBIDDEN);
		}
	}

	/**
	 * Ensures {@code screenRoute} matches an active menu and {@code actionName} is granted for that menu on
	 * {@code UM_ROLE_MENU_PERM}.
	 */
	private void assertWorkflowActionForScreen(String screenRoute, String actionName, PermissionMetadataResponse meta) {
		String screen = screenRoute == null ? "" : screenRoute.trim();
		String action = actionName == null ? "" : actionName.trim();
		List<MenuPermissionRouteRow> menus = meta.getMenus() == null ? List.<MenuPermissionRouteRow>of() : meta.getMenus();
		MenuPermissionRouteRow hit = null;
		for (MenuPermissionRouteRow row : menus) {
			if (row.getRoute() != null && row.getRoute().trim().equalsIgnoreCase(screen)) {
				hit = row;
				break;
			}
		}
		if (hit == null) {
			throw new ServiceException("screenName must match the route of an active UM menu.", HttpStatus.BAD_REQUEST);
		}
		Map<Long, List<String>> byMenu = meta.getActionsByMenuId() == null ? Map.of() : meta.getActionsByMenuId();
		Long mid = hit.getMenuId();
		List<String> allowed = mid == null ? List.of() : byMenu.getOrDefault(mid, List.of());
		if (allowed.isEmpty()) {
			throw new ServiceException(
					"No UM_ROLE_MENU_PERM rows grant any action for this menu. Configure role menu permissions first.",
					HttpStatus.BAD_REQUEST);
		}
		boolean ok = allowed.stream().anyMatch(x -> x != null && x.equalsIgnoreCase(action));
		if (!ok) {
			throw new ServiceException(
					"actionName is not among the actions currently granted on UM_ROLE_MENU_PERM for this menu.",
					HttpStatus.BAD_REQUEST);
		}
	}

	private void validateCheckerPresent(WorkflowConfig cfg) {
		List<List<String>> rt = parseTierMatrix(cfg.getCheckerRolesJson());
		List<List<String>> ut = parseTierMatrix(cfg.getCheckerUsersJson());
		int tiers = Math.max(Math.max(1, rt.size()), ut.size());
		boolean ok = false;
		for (int i = 0; i < tiers; i++) {
			List<String> r = i < rt.size() ? rt.get(i) : List.of();
			List<String> u = i < ut.size() ? ut.get(i) : List.of();
			if (!r.isEmpty() || !u.isEmpty()) {
				ok = true;
				break;
			}
		}
		if (!ok) {
			throw new ServiceException("Each workflow must define at least one checker (role or user).",
					HttpStatus.BAD_REQUEST);
		}
	}

	private void assertConfigVisibility(WorkflowConfig cfg) {
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			return;
		}
		Long ctx = BusinessContextHolder.currentBusinessId();
		Long cb = cfg.getBusinessId();
		if (cb == null || (ctx != null && ctx.equals(cb))) {
			return;
		}
		throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
	}

	private void validateFlexibleJsonArray(String json, String field) {
		if (json == null || json.isBlank()) {
			return;
		}
		try {
			JsonNode n = objectMapper.readTree(json);
			if (!n.isArray()) {
				throw new ServiceException(field + " must be a JSON array", HttpStatus.BAD_REQUEST);
			}
			if (n.size() > 0 && n.get(0).isArray()) {
				for (JsonNode tier : n) {
					if (!tier.isArray()) {
						throw new ServiceException(field + " must be an array of string arrays", HttpStatus.BAD_REQUEST);
					}
					for (JsonNode cell : tier) {
						if (!cell.isTextual()) {
							throw new ServiceException(field + " tiers must contain strings only", HttpStatus.BAD_REQUEST);
						}
					}
				}
			} else {
				for (JsonNode cell : n) {
					if (!cell.isTextual()) {
						throw new ServiceException(field + " must contain only strings", HttpStatus.BAD_REQUEST);
					}
				}
			}
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			throw new ServiceException(field + " must be valid JSON", HttpStatus.BAD_REQUEST);
		}
	}

	private WorkflowConfigRowDto toConfigRow(WorkflowConfig c) {
		WorkflowConfigRowDto d = new WorkflowConfigRowDto();
		d.setId(c.getId());
		d.setScreenName(c.getScreenName());
		d.setActionName(c.getActionName());
		d.setHasWorkflow(c.isHasWorkflow());
		d.setLevelCount(c.getLevelCount() == null ? 1 : c.getLevelCount());
		d.setMakerRolesJson(c.getMakerRolesJson());
		d.setCheckerRolesJson(c.getCheckerRolesJson());
		d.setMakerUsersJson(c.getMakerUsersJson());
		d.setCheckerUsersJson(c.getCheckerUsersJson());
		d.setBuiltInKey(c.getBuiltInKey());
		d.setBusinessId(c.getBusinessId());
		d.setCreatedBy(c.getCreatedBy());
		d.setCreatedAt(c.getCreatedAt());
		d.setSystemLocked(c.getBuiltInKey() != null && !c.getBuiltInKey().isBlank());
		return d;
	}

	private void assertBusinessScopeForAction(WorkflowInstance inst) {
		Long ctx = BusinessContextHolder.currentBusinessId();
		Long instBiz = inst.getBusinessId();
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			if (ctx == null) {
				return;
			}
			if (instBiz == null || ctx.equals(instBiz)) {
				return;
			}
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
		if (BusinessContextHolder.canBypassTenant()) {
			if (ctx == null) {
				WorkflowConfig c = configRepository.findById(inst.getWorkflowConfigId())
						.orElseThrow(() -> new ServiceException("Workflow configuration missing", HttpStatus.NOT_FOUND));
				if (c.getBuiltInKey() != null && !c.getBuiltInKey().isBlank()) {
					return;
				}
				throw new ServiceException("Select a business context to act on this workflow request.",
						HttpStatus.BAD_REQUEST);
			}
			if (!ctx.equals(instBiz)) {
				throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
			}
			return;
		}
		if (ctx == null || !ctx.equals(instBiz)) {
			throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
		}
	}

	private String currentUsername() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		return a == null ? null : a.getName();
	}

	private List<String> currentRoleNames() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		if (a == null) {
			return List.of();
		}
		List<String> out = new ArrayList<>();
		for (GrantedAuthority ga : a.getAuthorities()) {
			String r = ga.getAuthority();
			if (r != null && r.regionMatches(true, 0, "ROLE_", 0, 5)) {
				r = r.substring(5);
			}
			out.add(r.toUpperCase());
		}
		return out;
	}

	private void applyDerivedLevelCount(WorkflowConfig cfg) {
		List<List<String>> rt = parseTierMatrix(cfg.getCheckerRolesJson());
		List<List<String>> ut = parseTierMatrix(cfg.getCheckerUsersJson());
		int n = Math.max(rt.size(), ut.size());
		if (n < 1) {
			n = 1;
		}
		if (n > 10) {
			throw new ServiceException("At most 10 checker levels are supported.", HttpStatus.BAD_REQUEST);
		}
		cfg.setLevelCount(n);
	}

	/**
	 * Parses either a legacy flat string array {@code ["A","B"]} or tiered
	 * {@code [["A"],["B","C"]]}.
	 */
	private List<List<String>> parseTierMatrix(String json) {
		List<List<String>> tiers = new ArrayList<>();
		if (json == null || json.isBlank()) {
			return tiers;
		}
		try {
			JsonNode root = objectMapper.readTree(json);
			if (!root.isArray() || root.size() == 0) {
				return tiers;
			}
			if (root.get(0).isArray()) {
				for (JsonNode tier : root) {
					List<String> row = new ArrayList<>();
					if (tier.isArray()) {
						for (JsonNode n : tier) {
							if (n.isTextual()) {
								row.add(n.asText().trim());
							}
						}
					}
					tiers.add(row);
				}
			} else {
				List<String> one = new ArrayList<>();
				for (JsonNode n : root) {
					if (n.isTextual()) {
						one.add(n.asText().trim());
					}
				}
				if (!one.isEmpty()) {
					tiers.add(one);
				}
			}
		} catch (Exception ignored) {
			return List.of();
		}
		return tiers;
	}

	private boolean isCheckerForTier(WorkflowConfig cfg, List<String> roleNamesUpper, String username,
			Integer tier1Based, Long businessId) {
		return workflowEnforcement.isCheckerForTier(cfg, roleNamesUpper, username, tier1Based, businessId);
	}

	private PermissionMetadataResponse permissionMetadataForPrincipal() {
		if (BusinessContextHolder.canBypassTenant()) {
			return menuPermissionMetadataService.buildMetadata();
		}
		return menuPermissionMetadataService
				.buildMetadataForRoleIds(menuPermissionService.resolveAllAssignedRoleIds());
	}
}
