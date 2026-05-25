package com.travel.api.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.travel.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.NOTIF_INBOX_TABLE, schema = DatabaseConstants.SCHEMA)
public class NotifInbox {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id")
	private Long businessId;

	@Column(nullable = false, length = 200)
	private String username;

	@Column(nullable = false, length = 40)
	private String category;

	@Column(nullable = false, length = 16)
	private String severity;

	@Column(nullable = false, length = 300)
	private String title;

	@Column(length = 2000)
	private String body;

	@Column(name = "link_route", length = 400)
	private String linkRoute;

	@Column(name = "resource_type", length = 64)
	private String resourceType;

	@Column(name = "resource_id", length = 64)
	private String resourceId;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getLinkRoute() {
		return linkRoute;
	}

	public void setLinkRoute(String linkRoute) {
		this.linkRoute = linkRoute;
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

	public LocalDateTime getReadAt() {
		return readAt;
	}

	public void setReadAt(LocalDateTime readAt) {
		this.readAt = readAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
