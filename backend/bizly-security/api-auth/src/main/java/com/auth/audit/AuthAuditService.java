package com.auth.audit;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth.api.model.user.UserEntity;

/**
 * Persists auth events to {@code UM.UM_AUDIT_LOG} (same table as UM service audits).
 */
@Service
public class AuthAuditService {

	private static final Logger log = LoggerFactory.getLogger(AuthAuditService.class);

	@Autowired
	private UmAuditLogRepository auditLogRepository;

	public void recordLogin(UserEntity user, String sessionId, String ipAddress, String deviceId, String requestPath) {
		record(AuditConstants.ACTION_LOGIN, user, sessionId, ipAddress, deviceId, requestPath, "POST");
	}

	public void recordLogout(UserEntity user, String sessionId, String ipAddress, String deviceId, String requestPath) {
		record(AuditConstants.ACTION_LOGOUT, user, sessionId, ipAddress, deviceId, requestPath, "POST");
	}

	private void record(String actionCode, UserEntity user, String sessionId, String ipAddress, String deviceId,
			String requestPath, String httpMethod) {
		if (user == null) {
			return;
		}
		try {
			UmAuditLog row = new UmAuditLog();
			row.setBusinessId(user.getBusinessId());
			row.setUsername(user.getUsername());
			row.setActionCode(actionCode);
			row.setResourceType(AuditConstants.RESOURCE_AUTH);
			row.setResourceId(sessionId);
			row.setHttpMethod(httpMethod);
			row.setRequestPath(requestPath);
			row.setIpAddress(ipAddress);
			row.setSessionId(sessionId);
			if (deviceId != null && !deviceId.isBlank()) {
				row.setNewValues("deviceId=" + deviceId);
			}
			row.setCreatedAt(LocalDateTime.now());
			auditLogRepository.save(row);
		} catch (Exception ex) {
			log.warn("[AUTH_AUDIT] Failed to persist {} for user={}: {}", actionCode, user.getUsername(),
					ex.getMessage());
		}
	}
}
