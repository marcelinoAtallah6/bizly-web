package com.um.api.service.audit;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.um.api.dto.audit.AuditLogRowResponse;
import com.um.api.dto.audit.GetsAuditLogsRequest;
import com.um.api.model.audit.UmAuditLog;
import com.um.api.repository.audit.UmAuditLogRepository;
import com.um.common.PageResponse;

@Service
public class AuditLogServiceImpl implements IAuditLogService {

	@Autowired
	private UmAuditLogRepository auditLogRepository;

	@Override
	public PageResponse<AuditLogRowResponse> search(GetsAuditLogsRequest request) {

		PageRequest pr = PageRequest.of(request.getPageNumber(), request.getPageSize());

		Page<UmAuditLog> page;
		String u = request.getUsernameContains();
		if (u != null && !u.isBlank()) {
			page = auditLogRepository.findByUsernameContainingIgnoreCaseOrderByCreatedAtDesc(u.trim(), pr);
		} else {
			page = auditLogRepository.findAllByOrderByCreatedAtDesc(pr);
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
