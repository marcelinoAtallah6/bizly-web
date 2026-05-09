package com.um.api.model.audit;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.um.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.AUDIT_LOG_TABLE)
public class UmAuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "um_audit_log_seq")
	@SequenceGenerator(name = "um_audit_log_seq", sequenceName = DatabaseConstants.AUDIT_LOG_SEQ, allocationSize = 1)
	private Long id;

	@Column(name = "username", length = 128)
	private String username;

	@Column(name = "action_code", nullable = false, length = 128)
	private String actionCode;

	@Column(name = "resource_type", length = 128)
	private String resourceType;

	@Column(name = "resource_id", length = 128)
	private String resourceId;

	@Lob
	@Column(name = "old_values")
	private String oldValues;

	@Lob
	@Column(name = "new_values")
	private String newValues;

	@Column(name = "http_method", length = 16)
	private String httpMethod;

	@Column(name = "request_path", length = 512)
	private String requestPath;

	@Column(name = "ip_address", length = 64)
	private String ipAddress;

	@Column(name = "session_id", length = 128)
	private String sessionId;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getOldValues() {
		return oldValues;
	}

	public void setOldValues(String oldValues) {
		this.oldValues = oldValues;
	}

	public String getNewValues() {
		return newValues;
	}

	public void setNewValues(String newValues) {
		this.newValues = newValues;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getRequestPath() {
		return requestPath;
	}

	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
