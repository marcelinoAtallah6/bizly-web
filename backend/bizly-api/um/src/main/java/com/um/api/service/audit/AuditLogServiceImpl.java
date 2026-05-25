package com.um.api.service.audit;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.um.api.dto.audit.AuditLogRowResponse;
import com.um.api.dto.audit.GetsAuditLogsRequest;
import com.um.api.model.audit.UmAuditLog;
import com.um.api.model.user.User;
import com.um.api.repository.audit.UmAuditLogRepository;
import com.um.api.repository.user.UserRepository;
import com.um.api.service.role.RolePolicyService;
import com.um.common.ApiMessages;
import com.um.common.PageResponse;
import com.um.exception.ServiceException;
import com.um.security.BusinessContextHolder;

@Service
public class AuditLogServiceImpl implements IAuditLogService {

	@Autowired
	private UmAuditLogRepository auditLogRepository;
	@Autowired
	private RolePolicyService rolePolicyService;
	@Autowired
	private UserRepository userRepository;

	@Override
	public PageResponse<AuditLogRowResponse> search(GetsAuditLogsRequest request) {

		PageRequest pr = PageRequest.of(request.getPageNumber(), request.getPageSize());
		String usernameFilter = request.getUsernameContains();
		boolean hasUsername = usernameFilter != null && !usernameFilter.isBlank();
		String username = hasUsername ? usernameFilter.trim() : null;

		Page<UmAuditLog> page;
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			Long scopedBusinessId = BusinessContextHolder.currentBusinessId();
			if (scopedBusinessId == null) {
				page = hasUsername
						? auditLogRepository.findByUsernameContainingIgnoreCaseOrderByCreatedAtDesc(username, pr)
						: auditLogRepository.findAllByOrderByCreatedAtDesc(pr);
			} else {
				page = queryBusinessAudit(scopedBusinessId, null, username, hasUsername, pr);
			}
		} else {
			Long businessId = BusinessContextHolder.currentBusinessId();
			if (businessId == null) {
				throw new ServiceException(ApiMessages.MENU_PERMISSION_DENIED, HttpStatus.FORBIDDEN);
			}
			List<String> visibleUsernames = rolePolicyService.visibleAuditableUsernames(currentActorUserId(),
					businessId);
			page = queryBusinessAudit(businessId, visibleUsernames, username, hasUsername, pr);
		}

		List<AuditLogRowResponse> rows = page.getContent().stream().map(this::toRow).collect(Collectors.toList());

		PageResponse<AuditLogRowResponse> out = new PageResponse<>();
		out.setItems(rows);
		out.setTotalCount(page.getTotalElements());
		out.setPageNumber(page.getNumber());
		out.setPageSize(page.getSize());
		out.setTotalPages(page.getTotalPages());
		return out;
	}

	private Page<UmAuditLog> queryBusinessAudit(Long businessId, List<String> visibleUsernames, String username,
			boolean hasUsername, PageRequest pr) {
		if (visibleUsernames != null && visibleUsernames.isEmpty()) {
			return Page.empty(pr);
		}
		if (visibleUsernames == null) {
			if (hasUsername) {
				return auditLogRepository.findByBusinessIdAndUsernameContainingIgnoreCaseOrderByCreatedAtDesc(
						businessId, username, pr);
			}
			return auditLogRepository.findByBusinessIdOrderByCreatedAtDesc(businessId, pr);
		}
		if (hasUsername) {
			return auditLogRepository.findByBusinessIdAndUsernameInAndUsernameContainingIgnoreCaseOrderByCreatedAtDesc(
					businessId, visibleUsernames, username, pr);
		}
		return auditLogRepository.findByBusinessIdAndUsernameInOrderByCreatedAtDesc(businessId, visibleUsernames, pr);
	}

	private Long currentActorUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
			return null;
		}
		return userRepository.findFirstByUsernameOrderByIdAsc(auth.getName()).map(User::getId).orElse(null);
	}

	private AuditLogRowResponse toRow(UmAuditLog e) {
		AuditLogRowResponse r = new AuditLogRowResponse();
		r.setId(e.getId());
		r.setUsername(e.getUsername());
		r.setActionCode(e.getActionCode());
		r.setResourceType(e.getResourceType());
		r.setResourceId(e.getResourceId());
		r.setOldValues(e.getOldValues());
		r.setNewValues(e.getNewValues());
		r.setHttpMethod(e.getHttpMethod());
		r.setRequestPath(e.getRequestPath());
		r.setIpAddress(e.getIpAddress());
		r.setSessionId(e.getSessionId());
		r.setCreatedAt(e.getCreatedAt());
		return r;
	}
}
