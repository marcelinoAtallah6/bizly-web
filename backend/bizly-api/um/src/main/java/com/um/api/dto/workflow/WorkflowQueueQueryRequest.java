package com.um.api.dto.workflow;

public class WorkflowQueueQueryRequest {

	private String status;

	/** Case-insensitive substring match against {@code screen_name}. */
	private String screenNameContains;

	/** Case-insensitive substring match against {@code action_name}. */
	private String actionNameContains;

	/**
	 * Exact match (case-insensitive) against {@code screen_name} (menu route stored on the
	 * instance). Prefer this for dropdown filters.
	 */
	private String screenRoute;

	/**
	 * Exact match (case-insensitive) against {@code action_name} (permission verb from
	 * {@code UM_ROLE_MENU_PERM}). Prefer this for dropdown filters.
	 */
	private String actionName;

	/** Inclusive start date (ISO-8601 date, e.g. {@code 2026-05-01}) on {@code created_at}. */
	private String dateFrom;

	/** Inclusive end date on {@code created_at}. */
	private String dateTo;

	/** Case-insensitive substring match against {@code triggered_by_username}. */
	private String makerUsernameContains;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getScreenNameContains() {
		return screenNameContains;
	}

	public void setScreenNameContains(String screenNameContains) {
		this.screenNameContains = screenNameContains;
	}

	public String getActionNameContains() {
		return actionNameContains;
	}

	public void setActionNameContains(String actionNameContains) {
		this.actionNameContains = actionNameContains;
	}

	public String getScreenRoute() {
		return screenRoute;
	}

	public void setScreenRoute(String screenRoute) {
		this.screenRoute = screenRoute;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getMakerUsernameContains() {
		return makerUsernameContains;
	}

	public void setMakerUsernameContains(String makerUsernameContains) {
		this.makerUsernameContains = makerUsernameContains;
	}
}
