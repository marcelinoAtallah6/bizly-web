package com.broadcast.api.service.broadcast;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.broadcast.api.dto.broadcast.CreateBroadcastRequest;
import com.broadcast.api.dto.broadcast.CreateBroadcastResponse;
import com.broadcast.api.dto.broadcast.GetBroadcastRequest;
import com.broadcast.api.dto.broadcast.GetBroadcastResponse;
import com.broadcast.api.dto.broadcast.GetsBroadcastsRequest;
import com.broadcast.api.dto.broadcast.PreviewBroadcastRequest;
import com.broadcast.api.dto.broadcast.PreviewBroadcastResponse;
import com.broadcast.api.dto.broadcast.SendBroadcastRequest;
import com.broadcast.api.dto.broadcast.SendBroadcastResponse;
import com.broadcast.api.enums.BroadcastStatus;
import com.broadcast.api.enums.BroadcastTargetType;
import com.broadcast.api.model.BroadcastMessage;
import com.broadcast.api.model.MailTemplateEntity;
import com.broadcast.api.repository.BroadcastMessageRepository;
import com.broadcast.api.repository.MailTemplateRepository;
import com.broadcast.api.util.TemplatePlaceholderRenderer;
import com.broadcast.common.ApiMessages;
import com.broadcast.common.NotificationTemplateCodes;
import com.broadcast.common.PageResponse;
import com.broadcast.exception.ServiceException;
import com.broadcast.security.BusinessContextHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BroadcastServiceImpl implements IBroadcastService {

	private static final Logger log = LogManager.getLogger(BroadcastServiceImpl.class);

	private static final String DEFAULT_BC_SUBJECT = "Message from Bizly";
	private static final String DEFAULT_BC_HTML = "<html><body><p>Hello {{name}},</p><p>{{promotion}}</p><p>— Bizly</p></body></html>";

	@Autowired
	private BroadcastMessageRepository broadcastMessageRepository;

	@Autowired
	private MailTemplateRepository mailTemplateRepository;

	@Autowired
	private TemplatePlaceholderRenderer templatePlaceholderRenderer;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	@Transactional
	public CreateBroadcastResponse create(CreateBroadcastRequest request, String createdByUsername) {
		assertBroadcastAdmin();
		BroadcastTargetType targetType = parseTargetType(request.getTargetType());
		validateTargetPayload(targetType, request.getTargetRoleId(), request.getCustomSegmentJson());

		BroadcastMessage row = new BroadcastMessage();
		/*
		 * Tenant scope: a broadcast belongs to the originating business. ADMIN callers acting
		 * without a business context (e.g. system-wide announcements) leave businessId NULL —
		 * those rows are visible across all tenants when consumed.
		 */
		row.setBusinessId(BusinessContextHolder.currentBusinessId());
		row.setSubject(request.getSubject().trim());
		row.setBody(request.getBody().trim());
		row.setTargetType(targetType.name());
		row.setTargetRoleId(request.getTargetRoleId());
		row.setCustomSegmentJson(trimToNull(request.getCustomSegmentJson()));
		row.setStatus(BroadcastStatus.PENDING.name());
		row.setDeliveryRequested(false);
		row.setCreatedBy(trimToNull(createdByUsername));

		broadcastMessageRepository.save(row);
		log.info("[BCAST][CREATE] id={} target={} by={}", row.getId(), row.getTargetType(), row.getCreatedBy());

		if (request.isQueueForSend()) {
			sendQueued(row.getId());
		}

		CreateBroadcastResponse out = new CreateBroadcastResponse();
		out.setId(row.getId());
		return out;
	}

	@Override
	public PreviewBroadcastResponse preview(PreviewBroadcastRequest request) {
		assertBroadcastAdmin();
		MailTemplateEntity tmpl = loadBroadcastTemplate();
		String subjectTmpl = firstNonBlank(request.getSubject(), tmpl != null ? tmpl.getSubjectTemplate() : null,
				DEFAULT_BC_SUBJECT);
		String bodyTmpl = firstNonBlank(request.getBody(), tmpl != null ? tmpl.getHtmlBody() : null, DEFAULT_BC_HTML);

		Map<String, String> vars = sampleVariables(request.getVariables());
		PreviewBroadcastResponse out = new PreviewBroadcastResponse();
		out.setSubject(templatePlaceholderRenderer.render(subjectTmpl, vars));
		String html = templatePlaceholderRenderer.render(bodyTmpl, vars);
		out.setHtmlBody(html);
		out.setTextBody(stripHtml(html));
		return out;
	}

	@Override
	@Transactional
	public SendBroadcastResponse send(SendBroadcastRequest request) {
		assertBroadcastAdmin();
		sendQueued(request.getId());
		SendBroadcastResponse out = new SendBroadcastResponse();
		out.setId(request.getId());
		out.setQueued(true);
		return out;
	}

	private void sendQueued(long id) {
		BroadcastMessage m = loadBroadcastForCaller(id);
		String st = m.getStatus() == null ? "" : m.getStatus().trim();
		if ("SENT".equalsIgnoreCase(st) || "PROCESSING".equalsIgnoreCase(st)) {
			throw new ServiceException(ApiMessages.BROADCAST_INVALID_STATE, HttpStatus.CONFLICT);
		}
		if ("FAILED".equalsIgnoreCase(st)) {
			throw new ServiceException(ApiMessages.BROADCAST_RESEND_NOT_ALLOWED, HttpStatus.CONFLICT);
		}
		if ("PENDING".equalsIgnoreCase(st) && m.isDeliveryRequested()) {
			throw new ServiceException(ApiMessages.BROADCAST_ALREADY_QUEUED, HttpStatus.CONFLICT);
		}
		if (!"PENDING".equalsIgnoreCase(st)) {
			throw new ServiceException(ApiMessages.BROADCAST_INVALID_STATE, HttpStatus.BAD_REQUEST);
		}
		m.setDeliveryRequested(true);
		broadcastMessageRepository.save(m);
		log.info("[BCAST][SEND] queued id={}", id);
	}

	@Override
	public GetBroadcastResponse get(GetBroadcastRequest request) {
		assertBroadcastAdmin();
		return toResponse(loadBroadcastForCaller(request.getId()));
	}

	/**
	 * Tenant-scoped loader. Business admins can only access broadcasts inside their tenant; the
	 * SUPER_ADMIN (role-level=ADMIN) is allowed to fetch any row across tenants for moderation.
	 */
	private BroadcastMessage loadBroadcastForCaller(long id) {
		if (BusinessContextHolder.canBypassTenant()) {
			return broadcastMessageRepository.findById(id)
					.orElseThrow(() -> new ServiceException(ApiMessages.BROADCAST_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
		Long businessId = BusinessContextHolder.requireBusinessId();
		return broadcastMessageRepository.findByIdAndBusinessId(id, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.BROADCAST_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	@Override
	public PageResponse<GetBroadcastResponse> gets(GetsBroadcastsRequest request) {
		assertBroadcastAdmin();
		Page<BroadcastMessage> page;
		if (BusinessContextHolder.canBypassTenant()) {
			page = broadcastMessageRepository.findAllByOrderByCreatedAtDesc(
					PageRequest.of(request.getPageNumber(), request.getPageSize()));
		} else {
			Long businessId = BusinessContextHolder.requireBusinessId();
			page = broadcastMessageRepository.findAllByBusinessIdOrderByCreatedAtDesc(businessId,
					PageRequest.of(request.getPageNumber(), request.getPageSize()));
		}
		List<GetBroadcastResponse> items = page.getContent().stream().map(this::toResponse).collect(Collectors.toList());
		PageResponse<GetBroadcastResponse> out = new PageResponse<>();
		out.setItems(items);
		out.setTotalCount(page.getTotalElements());
		out.setPageNumber(page.getNumber());
		out.setPageSize(page.getSize());
		out.setTotalPages(page.getTotalPages());
		return out;
	}

	private GetBroadcastResponse toResponse(BroadcastMessage m) {
		GetBroadcastResponse r = new GetBroadcastResponse();
		r.setId(m.getId());
		r.setSubject(m.getSubject());
		r.setBody(m.getBody());
		r.setTargetType(m.getTargetType());
		r.setTargetRoleId(m.getTargetRoleId());
		r.setCustomSegmentJson(m.getCustomSegmentJson());
		r.setStatus(m.getStatus());
		r.setDeliveryRequested(m.isDeliveryRequested());
		r.setCreatedAt(m.getCreatedAt());
		r.setSentAt(m.getSentAt());
		r.setCreatedBy(m.getCreatedBy());
		return r;
	}

	private MailTemplateEntity loadBroadcastTemplate() {
		/*
		 * Tenant-aware template lookup: prefer the caller's customised template, fall back to the
		 * global / system template when none has been overridden. Producers without a business
		 * context (system jobs) hit the legacy un-scoped finder so seed data still resolves.
		 */
		Long businessId = BusinessContextHolder.currentBusinessId();
		if (businessId != null) {
			java.util.List<MailTemplateEntity> rows = mailTemplateRepository.findForBusinessOrGlobal(
					NotificationTemplateCodes.BROADCAST_DEFAULT, "Y", businessId);
			return rows.isEmpty() ? null : rows.get(0);
		}
		return mailTemplateRepository
				.findFirstByTemplateKeyIgnoreCaseAndActiveIndIgnoreCaseOrderByIdAsc(
						NotificationTemplateCodes.BROADCAST_DEFAULT, "Y")
				.orElse(null);
	}

	private static String firstNonBlank(String... parts) {
		if (parts == null) {
			return "";
		}
		for (String p : parts) {
			if (p != null && !p.isBlank()) {
				return p.trim();
			}
		}
		return "";
	}

	private Map<String, String> sampleVariables(Map<String, String> fromRequest) {
		Map<String, String> vars = new HashMap<>();
		vars.put("name", "Sample User");
		vars.put("email", "user@example.com");
		vars.put("date", LocalDate.now().toString());
		vars.put("promotion", "Spring promotion: use code BIZLY10");
		if (fromRequest != null) {
			fromRequest.forEach((k, v) -> {
				if (k != null && v != null) {
					vars.put(k.trim(), v);
				}
			});
		}
		return vars;
	}

	private static String stripHtml(String html) {
		if (html == null) {
			return "";
		}
		return html.replaceAll("<[^>]+>", "");
	}

	private void validateTargetPayload(BroadcastTargetType type, Long roleId, String customJson) {
		switch (type) {
		case ROLE_BASED:
			if (roleId == null) {
				throw new ServiceException(ApiMessages.BROADCAST_VALIDATION, HttpStatus.BAD_REQUEST);
			}
			break;
		case CUSTOM_SEGMENT:
			validateCustomSegmentJson(customJson);
			break;
		default:
			break;
		}
	}

	private void validateCustomSegmentJson(String json) {
		if (json == null || json.isBlank()) {
			throw new ServiceException(ApiMessages.BROADCAST_VALIDATION, HttpStatus.BAD_REQUEST);
		}
		try {
			JsonNode root = objectMapper.readTree(json);
			boolean ok = false;
			if (root.isArray() && root.size() > 0) {
				ok = true;
			} else if (root.has("emails") && root.get("emails").isArray() && root.get("emails").size() > 0) {
				ok = true;
			}
			if (!ok) {
				throw new ServiceException(ApiMessages.BROADCAST_VALIDATION, HttpStatus.BAD_REQUEST);
			}
		} catch (ServiceException se) {
			throw se;
		} catch (Exception e) {
			throw new ServiceException(ApiMessages.BROADCAST_VALIDATION, HttpStatus.BAD_REQUEST);
		}
	}

	private static BroadcastTargetType parseTargetType(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new ServiceException(ApiMessages.BROADCAST_VALIDATION, HttpStatus.BAD_REQUEST);
		}
		try {
			return BroadcastTargetType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new ServiceException(ApiMessages.BROADCAST_VALIDATION, HttpStatus.BAD_REQUEST);
		}
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	private void assertBroadcastAdmin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !isBroadcastAdmin(auth)) {
			throw new ServiceException(ApiMessages.BROADCAST_ADMIN_REQUIRED, HttpStatus.FORBIDDEN);
		}
	}

	private static boolean isBroadcastAdmin(Authentication auth) {
		for (GrantedAuthority ga : auth.getAuthorities()) {
			String a = ga.getAuthority();
			if (a == null) {
				continue;
			}
			String upper = a.toUpperCase(Locale.ROOT);
			if (upper.contains("ADMIN")) {
				return true;
			}
		}
		return false;
	}
}
