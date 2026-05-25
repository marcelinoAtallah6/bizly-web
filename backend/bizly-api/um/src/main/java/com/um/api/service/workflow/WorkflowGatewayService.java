package com.um.api.service.workflow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.api.dto.menu.MenuPermissionRouteRow;
import com.um.api.dto.menu.PermissionMetadataResponse;
import com.um.api.dto.workflow.WorkflowEndpointCatalogRowDto;
import com.um.api.dto.workflow.gateway.UpdateWorkflowEndpointRequest;
import com.um.api.dto.workflow.gateway.WorkflowApiEndpointAdminDto;
import com.um.api.dto.workflow.gateway.WorkflowGatewayCaptureData;
import com.um.api.dto.workflow.gateway.WorkflowGatewayCaptureRequest;
import com.um.api.dto.workflow.gateway.WorkflowGatewayMatchRequest;
import com.um.api.dto.workflow.gateway.WorkflowGatewayMatchResponse;
import com.um.api.model.user.User;
import com.um.api.model.workflow.WorkflowApiEndpoint;
import com.um.api.model.workflow.WorkflowConfig;
import com.um.api.model.workflow.WorkflowInstance;
import com.um.api.repository.user.UserRepository;
import com.um.api.repository.workflow.WorkflowApiEndpointRepository;
import com.um.api.repository.workflow.WorkflowInstanceRepository;
import com.um.api.service.menu.MenuPermissionMetadataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.um.api.service.workflow.engine.WorkflowEngineActionCodes;
import com.um.api.service.workflow.engine.WorkflowEngineHttpNotificationService;
import com.um.api.service.workflow.engine.WorkflowEngineOrchestratorService;
import com.um.api.service.workflow.engine.WorkflowNotificationContextBuilder;
import com.um.common.ApiMessages;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@Service
public class WorkflowGatewayService {

	private static final Logger log = LogManager.getLogger(WorkflowGatewayService.class);

	private static final Set<String> KNOWN_SERVICE_PREFIXES = Set.of("bm", "pm", "kyc", "um", "settings", "broadcast");

	private final AntPathMatcher antPathMatcher = new AntPathMatcher();

	@Autowired
	private WorkflowApiEndpointRepository endpointRepository;
	@Autowired
	private WorkflowInstanceRepository instanceRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MenuPermissionMetadataService menuPermissionMetadataService;
	@Autowired
	private WorkflowEnforcementHelper workflowEnforcement;
	@Autowired
	private WorkflowEngineHttpNotificationService engineHttpNotificationService;
	@Autowired
	private WorkflowEngineOrchestratorService workflowEngineOrchestrator;
	@Autowired
	private WorkflowNotificationContextBuilder notificationContextBuilder;

	/**
	 * Runs published engine notification steps for an HTTP endpoint after a successful API call.
	 */
	public void notifyCompleted(String username, Long endpointId, Long businessId, Map<String, Object> context) {
		if (endpointId == null) {
			return;
		}
		if (!engineHttpNotificationService.hasPublishedPipelineForEndpoint(endpointId, businessId)) {
			log.info("[WF_GATEWAY] notify skipped — no published pipeline for EP:{}", endpointId);
			return;
		}
		Map<String, Object> ctx = context != null ? new LinkedHashMap<>(context) : new LinkedHashMap<>();
		if (username != null && !username.isBlank()) {
			ctx.putIfAbsent("username", username.trim());
		}
		Long biz = businessId != null ? businessId : BusinessContextHolder.currentBusinessId();
		workflowEngineOrchestrator.onActionCompleted(WorkflowEngineActionCodes.forEndpoint(endpointId), biz,
				notificationContextBuilder.enrich(ctx, biz));
		log.info("[WF_GATEWAY] notifyCompleted EP:{} businessId={}", endpointId, biz);
	}

	public WorkflowGatewayMatchResponse match(String username, WorkflowGatewayMatchRequest req) {
		String path = req.getPath() == null ? "" : req.getPath().trim();
		String method = req.getMethod() == null ? "" : req.getMethod().trim().toUpperCase(Locale.ROOT);
		Long businessId = req.getBusinessId();
		if (path.isEmpty() || method.isEmpty()) {
			return WorkflowGatewayMatchResponse.noMatch("path_and_method_required");
		}
		List<String> roleNamesUpper = currentRoleNamesUpper();
		List<WorkflowApiEndpoint> rules = endpointRepository.findActiveHttpEndpointsForGateway();
		for (WorkflowApiEndpoint ep : rules) {
			String pattern = ep.getPathAntPattern();
			if (pattern == null || pattern.isBlank()) {
				continue;
			}
			if (!httpMethodMatches(ep.getHttpMethod(), method)) {
				continue;
			}
			if (!antPathMatcher.match(pattern.trim(), path)) {
				continue;
			}
			long endpointId = ep.getId();
			boolean engineApproval = engineHttpNotificationService.hasActiveApprovalStepForEndpoint(endpointId,
					businessId);
			if (engineApproval) {
				Optional<WorkflowConfig> cfg = workflowEnforcement.resolveActiveWorkflow(ep.getScreenRoute(),
						ep.getActionCode(), businessId);
				if (cfg.isPresent()) {
					Long hierarchyBiz = businessId != null ? businessId : BusinessContextHolder.currentBusinessId();
					if (workflowEnforcement.shouldDeferMutatingRequest(cfg.get(), roleNamesUpper, username,
							hierarchyBiz)) {
						return WorkflowGatewayMatchResponse.defer(endpointId, ep.getScreenRoute(), ep.getActionCode());
					}
				}
				return WorkflowGatewayMatchResponse.endpointMatched(endpointId);
			}
			if (engineHttpNotificationService.hasPublishedPipelineForEndpoint(endpointId, businessId)) {
				return WorkflowGatewayMatchResponse.endpointMatched(endpointId);
			}
			Optional<WorkflowConfig> legacyCfg = workflowEnforcement.resolveActiveWorkflow(ep.getScreenRoute(),
					ep.getActionCode(), businessId);
			if (legacyCfg.isPresent()) {
				Long hierarchyBiz = businessId != null ? businessId : BusinessContextHolder.currentBusinessId();
				if (workflowEnforcement.shouldDeferMutatingRequest(legacyCfg.get(), roleNamesUpper, username,
						hierarchyBiz)) {
					return WorkflowGatewayMatchResponse.defer(endpointId, ep.getScreenRoute(), ep.getActionCode());
				}
			}
			return WorkflowGatewayMatchResponse.endpointMatched(endpointId);
		}
		return WorkflowGatewayMatchResponse.noMatch("no_matching_active_endpoint_and_workflow");
	}

	@Transactional
	public WorkflowGatewayCaptureData capture(String username, WorkflowGatewayCaptureRequest req) {
		User user = userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		WorkflowApiEndpoint ep = endpointRepository.findById(req.getEndpointId())
				.orElseThrow(() -> new ServiceException("Unknown workflow API endpoint", HttpStatus.BAD_REQUEST));
		if (!ep.isActive() || ep.getPathAntPattern() == null || ep.getPathAntPattern().isBlank()) {
			throw new ServiceException("Endpoint is not active or has no path pattern", HttpStatus.BAD_REQUEST);
		}
		Long ctxBid = BusinessContextHolder.currentBusinessId();
		boolean bypass = BusinessContextHolder.canBypassTenant();
		Long targetBid = req.getBusinessId() != null ? req.getBusinessId() : ctxBid;
		if (!bypass) {
			if (ctxBid == null || targetBid == null || !ctxBid.equals(targetBid)) {
				throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
			}
		}
		if (targetBid == null) {
			throw new ServiceException("businessId is required for deferred workflow capture", HttpStatus.BAD_REQUEST);
		}
		WorkflowConfig cfg = workflowEnforcement.resolveActiveWorkflow(ep.getScreenRoute(), ep.getActionCode(), targetBid)
				.orElseThrow(() -> new ServiceException("No active workflow configuration for this operation",
						HttpStatus.BAD_REQUEST));
		if (cfg.getId() == null) {
			throw new ServiceException(
					"Workflow is not linked to a persisted configuration. Republish the workflow engine pipeline.",
					HttpStatus.CONFLICT);
		}
		List<String> roleNamesUpper = currentRoleNamesUpper();
		if (!workflowEnforcement.shouldDeferMutatingRequest(cfg, roleNamesUpper, username, targetBid)) {
			throw new ServiceException("Workflow does not apply to this user for this operation",
					HttpStatus.BAD_REQUEST);
		}

		String headersJson;
		try {
			headersJson = sanitizeHeadersForStorage(req.getHeaders());
		} catch (JsonProcessingException e) {
			throw new ServiceException("Could not serialise headers", HttpStatus.BAD_REQUEST);
		}
		String payloadMeta;
		try {
			Map<String, Object> meta = new LinkedHashMap<>();
			meta.put("deferredGatewayCapture", Boolean.TRUE);
			meta.put("path", req.getPath());
			meta.put("method", req.getMethod());
			payloadMeta = objectMapper.writeValueAsString(meta);
		} catch (JsonProcessingException e) {
			throw new ServiceException("Could not serialise workflow metadata", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		WorkflowInstance inst = new WorkflowInstance();
		inst.setWorkflowConfigId(cfg.getId());
		inst.setBusinessId(targetBid);
		inst.setTriggeredByUserId(user.getId());
		inst.setTriggeredByUsername(username);
		inst.setCurrentLevel(1);
		inst.setStatus("PENDING");
		inst.setScreenName(cfg.getScreenName());
		inst.setActionName(cfg.getActionName());
		inst.setCreatedAt(LocalDateTime.now());
		inst.setUpdatedAt(LocalDateTime.now());
		inst.setPayloadJson(payloadMeta);
		inst.setEndpointId(ep.getId());
		inst.setCapturedHttpMethod(req.getMethod() == null ? null : req.getMethod().trim().toUpperCase(Locale.ROOT));
		inst.setCapturedHttpPath(req.getPath() == null ? null : req.getPath().trim());
		inst.setCapturedHttpQuery(req.getQuery());
		inst.setCapturedHeadersJson(headersJson);
		inst.setCapturedBody(req.getBody());
		Long id = instanceRepository.save(inst).getId();
		return new WorkflowGatewayCaptureData(id);
	}

	public List<WorkflowApiEndpointAdminDto> listEndpoints() {
		return endpointRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream().map(this::toAdminDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public void updateEndpoint(UpdateWorkflowEndpointRequest req) {
		WorkflowApiEndpoint ep = endpointRepository.findById(req.getId())
				.orElseThrow(() -> new ServiceException("Endpoint not found", HttpStatus.NOT_FOUND));
		if (req.getPathAntPattern() != null) {
			ep.setPathAntPattern(req.getPathAntPattern().isBlank() ? null : req.getPathAntPattern().trim());
		}
		if (req.getHttpMethod() != null && !req.getHttpMethod().isBlank()) {
			ep.setHttpMethod(req.getHttpMethod().trim().toUpperCase(Locale.ROOT));
		}
		if (req.getPriority() != null) {
			ep.setPriority(req.getPriority());
		}
		if (req.getActive() != null) {
			ep.setActive(Boolean.TRUE.equals(req.getActive()));
		}
		endpointRepository.save(ep);
	}

	@Transactional
	public int syncEndpointsFromMenusAndPermissions() {
		PermissionMetadataResponse meta = menuPermissionMetadataService.buildMetadata();
		Map<Long, List<String>> byMenu = meta.getActionsByMenuId() == null ? Map.of() : meta.getActionsByMenuId();
		List<MenuPermissionRouteRow> menus = meta.getMenus() == null ? List.<MenuPermissionRouteRow>of() : meta.getMenus();
		int touched = 0;
		for (MenuPermissionRouteRow row : menus) {
			Long mid = row.getMenuId();
			if (mid == null) {
				continue;
			}
			String route = row.getRoute();
			if (route == null || route.isBlank()) {
				continue;
			}
			String sk = extractServiceKey(route.trim());
			if (sk == null) {
				continue;
			}
			List<String> actions = byMenu.getOrDefault(mid, List.of());
			for (String action : actions) {
				if (action == null || action.isBlank()) {
					continue;
				}
				Optional<WorkflowApiEndpoint> existing = endpointRepository.findByMenuIdAndActionCodeIgnoreCase(mid,
						action.trim());
				WorkflowApiEndpoint ep = existing.orElseGet(WorkflowApiEndpoint::new);
				ep.setMenuId(mid);
				ep.setScreenRoute(route.trim());
				ep.setActionCode(action.trim().toUpperCase(Locale.ROOT));
				ep.setServiceKey(sk);
				if (ep.getHttpMethod() == null || ep.getHttpMethod().isBlank()) {
					ep.setHttpMethod("*");
				}
				if (ep.getPriority() == null) {
					ep.setPriority(0);
				}
				if (ep.getCreatedAt() == null) {
					ep.setCreatedAt(LocalDateTime.now());
				}
				if (ep.getId() == null) {
					ep.setActive(false);
					ep.setPathAntPattern(null);
				}
				endpointRepository.save(ep);
				touched++;
			}
		}
		return touched;
	}

	/**
	 * Mutating (ADD/EDIT/DELETE) HTTP endpoints that are active and have an Ant path — used by the workflow
	 * configuration UI instead of rebuilding screen/action from the permission matrix alone.
	 */
	public List<WorkflowEndpointCatalogRowDto> mutatingEndpointCatalogForWorkflowForm(String username) {
		userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		Map<String, String> labelByRoute = buildMenuLabelByRoute();
		List<WorkflowEndpointCatalogRowDto> out = new ArrayList<>();
		for (WorkflowApiEndpoint e : endpointRepository.findActiveHttpEndpointsForGateway()) {
			WorkflowEndpointCatalogRowDto d = new WorkflowEndpointCatalogRowDto();
			d.setEndpointId(e.getId());
			String sr = e.getScreenRoute() == null ? "" : e.getScreenRoute().trim();
			d.setScreenRoute(sr);
			String ml = labelByRoute.getOrDefault(sr.toLowerCase(Locale.ROOT), sr);
			d.setMenuLabel(ml);
			d.setActionCode(e.getActionCode());
			d.setHttpMethod(e.getHttpMethod());
			d.setPathAntPattern(e.getPathAntPattern());
			String pat = e.getPathAntPattern() == null ? "" : e.getPathAntPattern().trim();
			d.setDisplayLabel(ml + " — " + e.getActionCode() + " (" + pat + ")");
			out.add(d);
		}
		return out;
	}

	private Map<String, String> buildMenuLabelByRoute() {
		PermissionMetadataResponse meta = menuPermissionMetadataService.buildMetadata();
		Map<String, String> m = new LinkedHashMap<>();
		for (MenuPermissionRouteRow r : meta.getMenus() == null ? List.<MenuPermissionRouteRow>of() : meta.getMenus()) {
			if (r.getRoute() == null || r.getRoute().isBlank()) {
				continue;
			}
			String key = r.getRoute().trim().toLowerCase(Locale.ROOT);
			String label = r.getMenuPath() != null && !r.getMenuPath().isBlank() ? r.getMenuPath() : r.getRoute();
			m.putIfAbsent(key, label);
		}
		return m;
	}

	private WorkflowApiEndpointAdminDto toAdminDto(WorkflowApiEndpoint ep) {
		WorkflowApiEndpointAdminDto d = new WorkflowApiEndpointAdminDto();
		d.setId(ep.getId());
		d.setServiceKey(ep.getServiceKey());
		d.setHttpMethod(ep.getHttpMethod());
		d.setPathAntPattern(ep.getPathAntPattern());
		d.setMenuId(ep.getMenuId());
		d.setScreenRoute(ep.getScreenRoute());
		d.setActionCode(ep.getActionCode());
		d.setPriority(ep.getPriority());
		d.setActive(ep.isActive());
		return d;
	}

	private static boolean httpMethodMatches(String configured, String actual) {
		if (configured == null || configured.isBlank()) {
			return false;
		}
		String c = configured.trim().toUpperCase(Locale.ROOT);
		if ("*".equals(c)) {
			return "POST".equals(actual) || "PUT".equals(actual) || "PATCH".equals(actual) || "DELETE".equals(actual);
		}
		return c.equals(actual);
	}

	private String sanitizeHeadersForStorage(Map<String, String> raw) throws JsonProcessingException {
		Map<String, String> safe = new LinkedHashMap<>();
		if (raw != null) {
			for (Map.Entry<String, String> e : raw.entrySet()) {
				if (e.getKey() == null || e.getValue() == null) {
					continue;
				}
				String k = e.getKey().trim();
				if (allowedHeaderKey(k)) {
					safe.put(k, e.getValue());
				}
			}
		}
		return objectMapper.writeValueAsString(safe);
	}

	private static boolean allowedHeaderKey(String k) {
		String u = k.toUpperCase(Locale.ROOT);
		if (u.equals("AUTHORIZATION") || u.equals("COOKIE") || u.equals("SET-COOKIE")) {
			return false;
		}
		if (u.startsWith("X-")) {
			return true;
		}
		return u.equals("CONTENT-TYPE") || u.equals("ACCEPT");
	}

	private static String extractServiceKey(String route) {
		if (!route.startsWith("/")) {
			return null;
		}
		int i = route.indexOf('/', 1);
		String seg = i < 0 ? route.substring(1) : route.substring(1, i);
		seg = seg.toLowerCase(Locale.ROOT);
		if (KNOWN_SERVICE_PREFIXES.contains(seg)) {
			return seg;
		}
		return null;
	}

	private List<String> currentRoleNamesUpper() {
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
			if (r != null && !r.isBlank()) {
				out.add(r.toUpperCase(Locale.ROOT));
			}
		}
		return out;
	}

}
