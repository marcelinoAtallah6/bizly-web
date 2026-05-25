package com.travel.api.service.notif;

public class NotifPublishRequest {

	private String username;
	private Long businessId;
	private String category;
	private NotifSeverity severity = NotifSeverity.INFO;
	private String title;
	private String body;
	private String linkRoute;
	private String resourceType;
	private String resourceId;

	public static NotifPublishRequest of(String username, String category, NotifSeverity severity, String title) {
		NotifPublishRequest r = new NotifPublishRequest();
		r.username = username;
		r.category = category;
		r.severity = severity;
		r.title = title;
		return r;
	}

	public NotifPublishRequest body(String body) {
		this.body = body;
		return this;
	}

	public NotifPublishRequest link(String linkRoute) {
		this.linkRoute = linkRoute;
		return this;
	}

	public NotifPublishRequest resource(String resourceType, String resourceId) {
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		return this;
	}

	public NotifPublishRequest businessId(Long businessId) {
		this.businessId = businessId;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public String getCategory() {
		return category;
	}

	public NotifSeverity getSeverity() {
		return severity;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public String getLinkRoute() {
		return linkRoute;
	}

	public String getResourceType() {
		return resourceType;
	}

	public String getResourceId() {
		return resourceId;
	}
}
