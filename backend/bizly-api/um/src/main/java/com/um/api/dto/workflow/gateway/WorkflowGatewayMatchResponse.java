package com.um.api.dto.workflow.gateway;

public class WorkflowGatewayMatchResponse {

	private boolean defer;
	private String reason;
	private Long endpointId;
	private String screenRoute;
	private String actionCode;

	public static WorkflowGatewayMatchResponse noMatch(String reason) {
		WorkflowGatewayMatchResponse r = new WorkflowGatewayMatchResponse();
		r.defer = false;
		r.reason = reason;
		return r;
	}

	/**
	 * Active workflow applies, but the current user is not among the configured maker roles/users — the
	 * mutating request must not be deferred (and must not execute); the gateway turns this into HTTP 403.
	 */
	public static WorkflowGatewayMatchResponse makerDenied() {
		WorkflowGatewayMatchResponse r = new WorkflowGatewayMatchResponse();
		r.defer = false;
		r.reason = "maker_denied";
		return r;
	}

	public static WorkflowGatewayMatchResponse defer(Long endpointId, String screenRoute, String actionCode) {
		WorkflowGatewayMatchResponse r = new WorkflowGatewayMatchResponse();
		r.defer = true;
		r.reason = "workflow_active";
		r.endpointId = endpointId;
		r.screenRoute = screenRoute;
		r.actionCode = actionCode;
		return r;
	}

	/** Path matched a registered endpoint; request proceeds and engine may run after HTTP success. */
	public static WorkflowGatewayMatchResponse endpointMatched(Long endpointId) {
		WorkflowGatewayMatchResponse r = new WorkflowGatewayMatchResponse();
		r.defer = false;
		r.reason = "endpoint_matched";
		r.endpointId = endpointId;
		return r;
	}

	public boolean isDefer() {
		return defer;
	}

	public void setDefer(boolean defer) {
		this.defer = defer;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Long getEndpointId() {
		return endpointId;
	}

	public void setEndpointId(Long endpointId) {
		this.endpointId = endpointId;
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
}
