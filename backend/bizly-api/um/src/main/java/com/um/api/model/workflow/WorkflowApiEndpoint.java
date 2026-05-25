package com.um.api.model.workflow;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.um.common.DatabaseConstants;

/**
 * Registry row: gateway path (Ant) + HTTP verb → UM menu screen route + permission verb, used to
 * defer mutating HTTP calls (202) and replay them after final approval.
 */
@Entity
@Table(name = DatabaseConstants.WORKFLOW_API_ENDPOINT_TABLE, schema = DatabaseConstants.SCHEMA)
public class WorkflowApiEndpoint {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_ep_seq")
	@SequenceGenerator(name = "wf_ep_seq", sequenceName = DatabaseConstants.WORKFLOW_API_ENDPOINT_SEQ, allocationSize = 1)
	private Long id;

	/** Downstream Eureka service id (bm, pm, kyc, …). */
	@Column(name = "service_key", nullable = false, length = 32)
	private String serviceKey;

	/**
	 * HTTP verb or {@code *} for any mutating method (POST, PUT, PATCH, DELETE).
	 */
	@Column(name = "http_method", nullable = false, length = 16)
	private String httpMethod;

	/** Ant-style pattern on the full gateway path, e.g. {@code /bm/product/**}. */
	@Column(name = "path_ant_pattern", length = 500)
	private String pathAntPattern;

	@Column(name = "menu_id")
	private Long menuId;

	@Column(name = "screen_route", nullable = false, length = 400)
	private String screenRoute;

	@Column(name = "action_code", nullable = false, length = 40)
	private String actionCode;

	@Column(name = "priority", nullable = false)
	private Integer priority = 0;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	/** Stable legacy key (e.g. USER_CREATED_SUCCESS); unique when set. */
	@Column(name = "engine_action_code", length = 80)
	private String engineActionCode;

	@Column(name = "display_name", length = 200)
	private String displayName;

	@Column(name = "description", length = 1000)
	private String description;

	@Column(name = "trigger_kind", length = 30)
	private String triggerKind = "IMMEDIATE";

	/** HTTP = gateway path; DOMAIN = internal domain event. */
	@Column(name = "trigger_source", length = 20)
	private String triggerSource = "HTTP";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getPathAntPattern() {
		return pathAntPattern;
	}

	public void setPathAntPattern(String pathAntPattern) {
		this.pathAntPattern = pathAntPattern;
	}

	public Long getMenuId() {
		return menuId;
	}

	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}

	public String getScreenRoute() {
		return screenRoute;
	}

	public void setScreenRoute(String screenRoute) {
		this.screenRoute = screenRoute;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getEngineActionCode() {
		return engineActionCode;
	}

	public void setEngineActionCode(String engineActionCode) {
		this.engineActionCode = engineActionCode;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTriggerKind() {
		return triggerKind;
	}

	public void setTriggerKind(String triggerKind) {
		this.triggerKind = triggerKind;
	}

	public String getTriggerSource() {
		return triggerSource;
	}

	public void setTriggerSource(String triggerSource) {
		this.triggerSource = triggerSource;
	}
}
